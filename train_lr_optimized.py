# -*- coding: utf-8 -*-
from pyspark.sql import SparkSession
from pyspark.sql.functions import when, col, trim, length, udf
from pyspark.sql.types import ArrayType, StringType
from pyspark.ml.feature import CountVectorizer, IDF
from pyspark.ml.classification import LogisticRegression
from pyspark.ml import Pipeline
from pyspark.ml.evaluation import BinaryClassificationEvaluator, MulticlassClassificationEvaluator
import os
import jieba

def main():
    spark = SparkSession.builder \
        .appName("TextClassification_LR_Optimized") \
        .master("local[*]") \
        .config("spark.driver.memory", "4g") \
        .config("spark.sql.legacy.json.allowEmptyString.enabled", "true") \
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")
    
    print("=" * 70)
    print("优化版逻辑回归文本分类模型训练")
    print("=" * 70)
    
    base_path = r"d:\A_Graduation_Project\project\p2_0"
    
    train_files = [
        os.path.join(base_path, "不违规.json"),
        os.path.join(base_path, "博彩.json"),
        os.path.join(base_path, "低俗色情.json"),
        os.path.join(base_path, "黑产广告.json"),
        os.path.join(base_path, "谩骂引战.json"),
        os.path.join(base_path, "欺诈.json"),
    ]
    test_file = os.path.join(base_path, "bench.json")
    
    print("\n[步骤1] 加载数据...")
    train_dfs = []
    for file_path in train_files:
        if os.path.exists(file_path):
            df = spark.read.option("multiLine", "true").json(file_path)
            train_dfs.append(df)
            print(f"  - 已加载: {os.path.basename(file_path)}")
    
    train_df = train_dfs[0]
    for df in train_dfs[1:]:
        train_df = train_df.union(df)
    
    test_df = spark.read.option("multiLine", "true").json(test_file)
    print(f"  - 已加载测试集: bench.json")
    
    print("\n[步骤2] 数据清洗...")
    train_df = train_df.filter(col("文本").isNotNull() & (trim(col("文本")) != ""))
    test_df = test_df.filter(col("文本").isNotNull() & (trim(col("文本")) != ""))
    
    train_df = train_df.withColumn(
        "label",
        when(col("标签") == "不违规", 0.0).otherwise(1.0)
    )
    test_df = test_df.withColumn(
        "label",
        when(col("标签") == "不违规", 0.0).otherwise(1.0)
    )
    
    train_total = train_df.count()
    train_pos = train_df.filter(col("label") == 1.0).count()
    train_neg = train_df.filter(col("label") == 0.0).count()
    test_total = test_df.count()
    test_pos = test_df.filter(col("label") == 1.0).count()
    test_neg = test_df.filter(col("label") == 0.0).count()
    
    print(f"  训练集: 总计{train_total}条, 违规{train_pos}条({train_pos/train_total*100:.1f}%), 不违规{train_neg}条({train_neg/train_total*100:.1f}%)")
    print(f"  测试集: 总计{test_total}条, 违规{test_pos}条({test_pos/test_total*100:.1f}%), 不违规{test_neg}条({test_neg/test_total*100:.1f}%)")
    
    print("\n[步骤3] 中文分词...")
    def chinese_tokenize(text):
        if text is None:
            return []
        words = jieba.cut(text)
        return [w.strip() for w in words if len(w.strip()) > 0]
    
    tokenize_udf = udf(chinese_tokenize, ArrayType(StringType()))
    train_df = train_df.withColumn("words", tokenize_udf(col("文本")))
    test_df = test_df.withColumn("words", tokenize_udf(col("文本")))
    
    print("  分词示例:")
    for row in train_df.select("文本", "words", "label").limit(3).collect():
        text_preview = row["文本"][:30] if len(row["文本"]) > 30 else row["文本"]
        words_preview = row["words"][:8]
        print(f"    '{text_preview}...' -> {words_preview}")
    
    print("\n[步骤4] 特征提取 (TF-IDF)...")
    countVectorizer = CountVectorizer(
        inputCol="words",
        outputCol="rawFeatures",
        vocabSize=10000,
        minDF=2.0
    )
    cv_model = countVectorizer.fit(train_df)
    train_df = cv_model.transform(train_df)
    test_df = cv_model.transform(test_df)
    
    vocab = cv_model.vocabulary
    print(f"  词汇表大小: {len(vocab)}")
    print(f"  高频词示例: {vocab[:15]}")
    
    idf = IDF(inputCol="rawFeatures", outputCol="features")
    idf_model = idf.fit(train_df)
    train_df = idf_model.transform(train_df)
    test_df = idf_model.transform(test_df)
    
    print("\n[步骤5] 处理数据不平衡...")
    imbalance_ratio = train_pos / train_neg
    print(f"  数据不平衡比例: {imbalance_ratio:.2f}:1")
    
    weight_for_0 = train_pos / train_total
    weight_for_1 = train_neg / train_total
    
    train_df = train_df.withColumn(
        "classWeight",
        when(col("label") == 0.0, weight_for_0).otherwise(weight_for_1)
    )
    
    print(f"  类别权重: 不违规={weight_for_0:.4f}, 违规={weight_for_1:.4f}")
    
    print("\n[步骤6] 训练逻辑回归模型...")
    lr = LogisticRegression(
        featuresCol="features",
        labelCol="label",
        weightCol="classWeight",
        maxIter=100,
        regParam=0.01,
        elasticNetParam=0.0,
        standardization=True
    )
    
    lr_model = lr.fit(train_df)
    print("  模型训练完成!")
    
    print("\n[步骤7] 模型评估...")
    predictions = lr_model.transform(test_df)
    
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
    
    print("\n" + "=" * 70)
    print("评估结果")
    print("=" * 70)
    print(f"  AUC-ROC:  {auc:.4f}")
    print(f"  准确率:   {accuracy:.4f}")
    print(f"\n  混淆矩阵:")
    print(f"              预测=不违规  预测=违规")
    print(f"  实际=不违规    {tn:5d}       {fp:5d}")
    print(f"  实际=违规      {fn:5d}       {tp:5d}")
    print(f"\n  不违规类(label=0):")
    print(f"    精确率: {precision_neg:.4f} (预测为不违规的样本中有多少是真正不违规)")
    print(f"    召回率: {recall_neg:.4f} (真正不违规的样本有多少被正确识别)")
    print(f"    F1分数: {f1_neg:.4f}")
    print(f"\n  违规类(label=1):")
    print(f"    精确率: {precision_pos:.4f} (预测为违规的样本中有多少是真正违规)")
    print(f"    召回率: {recall_pos:.4f} (真正违规的样本有多少被正确识别)")
    print(f"    F1分数: {f1_pos:.4f}")
    
    print("\n[步骤8] 错误分析...")
    print("\n  假阳性(FP) - 不违规被误判为违规:")
    fp_samples = predictions.filter((col("label") == 0.0) & (col("prediction") == 1.0)).select("文本").limit(5).collect()
    for i, row in enumerate(fp_samples):
        text = row["文本"][:50] if len(row["文本"]) > 50 else row["文本"]
        print(f"    {i+1}. '{text}...'")
    
    print("\n  假阴性(FN) - 违规被误判为不违规:")
    fn_samples = predictions.filter((col("label") == 1.0) & (col("prediction") == 0.0)).select("文本").limit(5).collect()
    for i, row in enumerate(fn_samples):
        text = row["文本"][:50] if len(row["文本"]) > 50 else row["文本"]
        print(f"    {i+1}. '{text}...'")
    
    print("\n[步骤9] 保存模型...")
    
    pipeline = Pipeline(stages=[
        countVectorizer,
        idf,
        lr
    ])
    
    train_df_raw = train_df.drop("words", "rawFeatures", "features", "classWeight")
    train_df_raw = train_df_raw.withColumn("words", tokenize_udf(col("文本")))
    train_df_raw = train_df_raw.withColumn(
        "classWeight",
        when(col("label") == 0.0, weight_for_0).otherwise(weight_for_1)
    )
    
    pipeline_model = pipeline.fit(train_df_raw)
    
    model_path = os.path.join(base_path, "lr_text_classification_model_optimized")
    pipeline_model.write().overwrite().save(model_path)
    print(f"  模型已保存至: {model_path}")
    
    print("\n[步骤10] 测试预测...")
    test_texts = [
        "今天天气真好，适合出去散步",
        "加我微信，带你赚钱，日入过万",
        "这个视频拍得很好看，学到了很多",
        "牛牛金花玩的加我，稳赚不赔",
        "你好，请问这个怎么使用？"
    ]
    
    from pyspark.sql import Row
    test_rdd = spark.sparkContext.parallelize([Row(文本=text) for text in test_texts])
    test_input_df = spark.createDataFrame(test_rdd)
    test_input_df = test_input_df.withColumn("words", tokenize_udf(col("文本")))
    
    test_predictions = pipeline_model.transform(test_input_df)
    
    print("\n  预测结果:")
    for row in test_predictions.select("文本", "prediction", "probability").collect():
        text = row["文本"][:30] if len(row["文本"]) > 30 else row["文本"]
        pred = "违规" if row["prediction"] == 1.0 else "不违规"
        prob = row["probability"]
        print(f"    '{text}...' -> {pred} (违规概率: {prob[1]:.4f})")
    
    spark.stop()
    print("\n" + "=" * 70)
    print("训练完成!")
    print("=" * 70)

if __name__ == "__main__":
    main()
