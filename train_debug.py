# -*- coding: utf-8 -*-
from pyspark.sql import SparkSession
from pyspark.sql.functions import when, col, length, trim
from pyspark.ml.feature import Tokenizer, HashingTF, IDF, StringIndexer, CountVectorizer
from pyspark.ml.classification import LogisticRegression, NaiveBayes, RandomForestClassifier, GBTClassifier
from pyspark.ml import Pipeline
from pyspark.ml.evaluation import BinaryClassificationEvaluator, MulticlassClassificationEvaluator
from pyspark.ml.tuning import ParamGridBuilder, CrossValidator
import os
import jieba

def main():
    spark = SparkSession.builder \
        .appName("TextClassification_Debug") \
        .master("local[*]") \
        .config("spark.driver.memory", "4g") \
        .config("spark.sql.legacy.json.allowEmptyString.enabled", "true") \
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")
    
    print("=" * 70)
    print("Spark 文本分类模型训练 - DEBUG模式")
    print("=" * 70)
    
    base_path = r"d:\A_Graduation_Project\project\p2_0"
    
    train_files = [
        (os.path.join(base_path, "不违规.json"), "不违规"),
        (os.path.join(base_path, "博彩.json"), "博彩"),
        (os.path.join(base_path, "低俗色情.json"), "低俗色情"),
        (os.path.join(base_path, "黑产广告.json"), "黑产广告"),
        (os.path.join(base_path, "谩骂引战.json"), "谩骂引战"),
        (os.path.join(base_path, "欺诈.json"), "欺诈"),
    ]
    
    test_file = os.path.join(base_path, "bench.json")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤1: 加载并检查原始数据")
    print("=" * 70)
    
    train_dfs = []
    for file_path, label_name in train_files:
        if os.path.exists(file_path):
            df = spark.read.option("multiLine", "true").json(file_path)
            count = df.count()
            print(f"  - {label_name}: {count} 条样本")
            
            print(f"    [DEBUG] Schema: {df.columns}")
            sample = df.limit(3).collect()
            for i, row in enumerate(sample):
                text = row["文本"][:50] if row["文本"] and len(row["文本"]) > 50 else row["文本"]
                print(f"    [DEBUG] 样本{i+1}: 文本='{text}...' 标签='{row['标签']}'")
            train_dfs.append(df)
        else:
            print(f"  - 文件不存在: {file_path}")
    
    train_df = train_dfs[0]
    for df in train_dfs[1:]:
        train_df = train_df.union(df)
    
    print(f"\n[DEBUG] 测试数据...")
    test_df = spark.read.option("multiLine", "true").json(test_file)
    print(f"  - 测试集总数: {test_df.count()}")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤2: 数据清洗")
    print("=" * 70)
    
    train_df = train_df.filter(col("文本").isNotNull() & (trim(col("文本")) != ""))
    test_df = test_df.filter(col("文本").isNotNull() & (trim(col("文本")) != ""))
    
    train_df = train_df.withColumn("text_length", length(col("文本")))
    test_df = test_df.withColumn("text_length", length(col("文本")))
    
    print(f"  - 训练集清洗后样本数: {train_df.count()}")
    print(f"  - 测试集清洗后样本数: {test_df.count()}")
    
    train_df.select("text_length").describe().show()
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤3: 创建二分类标签")
    print("=" * 70)
    
    train_df = train_df.withColumn(
        "label",
        when(col("标签") == "不违规", 0.0).otherwise(1.0)
    )
    test_df = test_df.withColumn(
        "label",
        when(col("标签") == "不违规", 0.0).otherwise(1.0)
    )
    
    train_pos = train_df.filter(col("label") == 1.0).count()
    train_neg = train_df.filter(col("label") == 0.0).count()
    test_pos = test_df.filter(col("label") == 1.0).count()
    test_neg = test_df.filter(col("label") == 0.0).count()
    
    print(f"  训练集分布:")
    print(f"    - 违规样本(label=1): {train_pos} ({train_pos/(train_pos+train_neg)*100:.1f}%)")
    print(f"    - 不违规样本(label=0): {train_neg} ({train_neg/(train_pos+train_neg)*100:.1f}%)")
    print(f"  测试集分布:")
    print(f"    - 违规样本(label=1): {test_pos} ({test_pos/(test_pos+test_neg)*100:.1f}%)")
    print(f"    - 不违规样本(label=0): {test_neg} ({test_neg/(test_pos+test_neg)*100:.1f}%)")
    
    print(f"\n  [WARNING] 数据不平衡比例: {train_pos/train_neg:.1f}:1")
    print(f"  [建议] 不平衡比例超过3:1时，需要处理数据不平衡问题")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤4: 中文分词处理")
    print("=" * 70)
    
    def chinese_tokenize(text):
        if text is None:
            return []
        words = jieba.cut(text)
        return [w for w in words if len(w.strip()) > 0]
    
    from pyspark.sql.types import ArrayType, StringType
    from pyspark.sql.functions import udf
    
    tokenize_udf = udf(chinese_tokenize, ArrayType(StringType()))
    
    train_df = train_df.withColumn("words", tokenize_udf(col("文本")))
    test_df = test_df.withColumn("words", tokenize_udf(col("文本")))
    
    print("  [DEBUG] 分词示例:")
    sample_words = train_df.select("文本", "words", "label").limit(5).collect()
    for i, row in enumerate(sample_words):
        text = row["文本"][:30] if len(row["文本"]) > 30 else row["文本"]
        words = row["words"][:10]
        print(f"    样本{i+1}: '{text}...' -> 分词: {words}")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤5: 特征提取")
    print("=" * 70)
    
    countVectorizer = CountVectorizer(
        inputCol="words",
        outputCol="rawFeatures",
        vocabSize=5000,
        minDF=2.0
    )
    
    cv_model = countVectorizer.fit(train_df)
    train_df = cv_model.transform(train_df)
    test_df = cv_model.transform(test_df)
    
    vocab = cv_model.vocabulary
    print(f"  - 词汇表大小: {len(vocab)}")
    print(f"  [DEBUG] 前20个高频词: {vocab[:20]}")
    
    idf = IDF(inputCol="rawFeatures", outputCol="features")
    idf_model = idf.fit(train_df)
    train_df = idf_model.transform(train_df)
    test_df = idf_model.transform(test_df)
    
    print("  - TF-IDF特征维度: 5000")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤6: 训练多个模型并比较")
    print("=" * 70)
    
    class_weight_ratio = train_neg / train_pos
    print(f"  - 计算类别权重: 不违规权重={class_weight_ratio:.4f}")
    
    models = {
        "逻辑回归(带权重)": LogisticRegression(
            featuresCol="features",
            labelCol="label",
            maxIter=100,
            regParam=0.01,
            weightCol=None,
            classWeight={0: class_weight_ratio, 1: 1.0} if hasattr(LogisticRegression, 'classWeight') else None
        ),
        "朴素贝叶斯": NaiveBayes(
            featuresCol="features",
            labelCol="label",
            smoothing=1.0
        ),
        "随机森林": RandomForestClassifier(
            featuresCol="features",
            labelCol="label",
            numTrees=50,
            maxDepth=10,
            seed=42
        ),
    }
    
    results = {}
    
    for model_name, model in models.items():
        print(f"\n  {'='*50}")
        print(f"  训练模型: {model_name}")
        print(f"  {'='*50}")
        
        try:
            trained_model = model.fit(train_df)
            predictions = trained_model.transform(test_df)
            
            evaluator_auc = BinaryClassificationEvaluator(
                rawPredictionCol="rawPrediction",
                labelCol="label",
                metricName="areaUnderROC"
            )
            auc = evaluator_auc.evaluate(predictions)
            
            evaluator_acc = MulticlassClassificationEvaluator(
                labelCol="label", predictionCol="prediction", metricName="accuracy"
            )
            accuracy = evaluator_acc.evaluate(predictions)
            
            tp = predictions.filter((col("label") == 1.0) & (col("prediction") == 1.0)).count()
            tn = predictions.filter((col("label") == 0.0) & (col("prediction") == 0.0)).count()
            fp = predictions.filter((col("label") == 0.0) & (col("prediction") == 1.0)).count()
            fn = predictions.filter((col("label") == 1.0) & (col("prediction") == 0.0)).count()
            
            precision_neg = tn / (tn + fn) if (tn + fn) > 0 else 0
            recall_neg = tn / (tn + fp) if (tn + fp) > 0 else 0
            f1_neg = 2 * precision_neg * recall_neg / (precision_neg + recall_neg) if (precision_neg + recall_neg) > 0 else 0
            
            precision_pos = tp / (tp + fp) if (tp + fp) > 0 else 0
            recall_pos = tp / (tp + fn) if (tp + fn) > 0 else 0
            f1_pos = 2 * precision_pos * recall_pos / (precision_pos + recall_pos) if (precision_pos + recall_pos) > 0 else 0
            
            results[model_name] = {
                "auc": auc,
                "accuracy": accuracy,
                "tp": tp, "tn": tn, "fp": fp, "fn": fn,
                "precision_neg": precision_neg,
                "recall_neg": recall_neg,
                "f1_neg": f1_neg,
                "precision_pos": precision_pos,
                "recall_pos": recall_pos,
                "f1_pos": f1_pos,
                "model": trained_model
            }
            
            print(f"    AUC-ROC: {auc:.4f}")
            print(f"    准确率: {accuracy:.4f}")
            print(f"    混淆矩阵: TP={tp}, TN={tn}, FP={fp}, FN={fn}")
            print(f"    不违规类(label=0): 精确率={precision_neg:.4f}, 召回率={recall_neg:.4f}, F1={f1_neg:.4f}")
            print(f"    违规类(label=1): 精确率={precision_pos:.4f}, 召回率={recall_pos:.4f}, F1={f1_pos:.4f}")
            
        except Exception as e:
            print(f"    [ERROR] 训练失败: {str(e)}")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤7: 模型对比总结")
    print("=" * 70)
    
    print(f"\n  {'模型名称':<20} {'AUC':<8} {'准确率':<8} {'不违规F1':<10} {'违规F1':<10}")
    print("  " + "-" * 60)
    for model_name, r in results.items():
        print(f"  {model_name:<20} {r['auc']:<8.4f} {r['accuracy']:<8.4f} {r['f1_neg']:<10.4f} {r['f1_pos']:<10.4f}")
    
    best_model_name = max(results.keys(), key=lambda x: results[x]['f1_neg'])
    print(f"\n  [推荐] 不违规类F1最高的模型: {best_model_name}")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 步骤8: 错误分析")
    print("=" * 70)
    
    best_predictions = results[best_model_name]["model"].transform(test_df)
    
    print("\n  [假阳性分析] 不违规被误判为违规的样本:")
    fp_samples = best_predictions.filter((col("label") == 0.0) & (col("prediction") == 1.0)).select("文本", "标签").limit(10).collect()
    for i, row in enumerate(fp_samples):
        text = row["文本"][:60] if len(row["文本"]) > 60 else row["文本"]
        print(f"    {i+1}. '{text}...'")
    
    print("\n  [假阴性分析] 违规被误判为不违规的样本:")
    fn_samples = best_predictions.filter((col("label") == 1.0) & (col("prediction") == 0.0)).select("文本", "标签").limit(10).collect()
    for i, row in enumerate(fn_samples):
        text = row["文本"][:60] if len(row["文本"]) > 60 else row["文本"]
        print(f"    {i+1}. '{text}...'")
    
    print("\n" + "=" * 70)
    print("[DEBUG] 总结与建议")
    print("=" * 70)
    print("""
  1. 数据不平衡问题:
     - 当前违规:不违规比例约为 8.5:1
     - 建议: 增加不违规样本数量，或使用过采样/欠采样技术
  
  2. 特征工程:
     - 当前使用jieba中文分词 + TF-IDF
     - 建议: 可以尝试Word2Vec、BERT等词向量方法
  
  3. 模型选择:
     - 逻辑回归: 简单快速，可解释性强
     - 朴素贝叶斯: 适合文本分类，训练速度快
     - 随机森林: 能处理非线性关系，但训练较慢
     - 建议: 可以尝试XGBoost、深度学习模型(BERT)
  
  4. 阈值调整:
     - 当前默认阈值0.5
     - 建议: 根据业务需求调整阈值，降低假阳性率
    """)
    
    spark.stop()
    print("\nDEBUG分析完成!")

if __name__ == "__main__":
    main()
