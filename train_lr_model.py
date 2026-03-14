# -*- coding: utf-8 -*-
from pyspark.sql import SparkSession
from pyspark.sql.functions import when, col
from pyspark.ml.feature import Tokenizer, HashingTF, IDF, StringIndexer
from pyspark.ml.classification import LogisticRegression
from pyspark.ml import Pipeline
from pyspark.ml.evaluation import BinaryClassificationEvaluator, MulticlassClassificationEvaluator
import os

def main():
    spark = SparkSession.builder \
        .appName("TextClassification_LR") \
        .master("local[*]") \
        .config("spark.driver.memory", "4g") \
        .config("spark.sql.legacy.json.allowEmptyString.enabled", "true") \
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")
    
    print("=" * 60)
    print("Spark 逻辑回归文本分类模型训练")
    print("=" * 60)
    
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
    
    print("\n[1] 加载训练数据...")
    train_dfs = []
    for file_path in train_files:
        if os.path.exists(file_path):
            df = spark.read.option("multiLine", "true").json(file_path)
            train_dfs.append(df)
            print(f"  - 已加载: {os.path.basename(file_path)}")
        else:
            print(f"  - 文件不存在: {file_path}")
    
    train_df = train_dfs[0]
    for df in train_dfs[1:]:
        train_df = train_df.union(df)
    
    print(f"\n[2] 加载测试数据...")
    test_df = spark.read.option("multiLine", "true").json(test_file)
    print(f"  - 已加载: {os.path.basename(test_file)}")
    
    print("\n[3] 数据预处理 - 创建二分类标签...")
    train_df = train_df.withColumn(
        "label",
        when(col("标签") == "不违规", 0.0).otherwise(1.0)
    )
    test_df = test_df.withColumn(
        "label",
        when(col("标签") == "不违规", 0.0).otherwise(1.0)
    )
    
    train_df = train_df.na.drop(subset=["文本", "label"])
    test_df = test_df.na.drop(subset=["文本", "label"])
    
    print(f"  - 训练集样本数: {train_df.count()}")
    print(f"  - 测试集样本数: {test_df.count()}")
    
    train_pos = train_df.filter(col("label") == 1.0).count()
    train_neg = train_df.filter(col("label") == 0.0).count()
    print(f"  - 训练集违规样本: {train_pos}, 不违规样本: {train_neg}")
    
    test_pos = test_df.filter(col("label") == 1.0).count()
    test_neg = test_df.filter(col("label") == 0.0).count()
    print(f"  - 测试集违规样本: {test_pos}, 不违规样本: {test_neg}")
    
    print("\n[4] 构建Pipeline...")
    tokenizer = Tokenizer(inputCol="文本", outputCol="words")
    hashingTF = HashingTF(inputCol="words", outputCol="rawFeatures", numFeatures=10000)
    idf = IDF(inputCol="rawFeatures", outputCol="features")
    lr = LogisticRegression(
        featuresCol="features",
        labelCol="label",
        maxIter=100,
        regParam=0.01,
        elasticNetParam=0.0
    )
    
    pipeline = Pipeline(stages=[tokenizer, hashingTF, idf, lr])
    
    print("\n[5] 训练模型...")
    model = pipeline.fit(train_df)
    print("  - 模型训练完成!")
    
    print("\n[6] 在测试集上评估模型...")
    predictions = model.transform(test_df)
    
    evaluator_auc = BinaryClassificationEvaluator(
        rawPredictionCol="rawPrediction",
        labelCol="label",
        metricName="areaUnderROC"
    )
    auc = evaluator_auc.evaluate(predictions)
    
    evaluator_acc = MulticlassClassificationEvaluator(
        labelCol="label",
        predictionCol="prediction",
        metricName="accuracy"
    )
    accuracy = evaluator_acc.evaluate(predictions)
    
    evaluator_precision = MulticlassClassificationEvaluator(
        labelCol="label",
        predictionCol="prediction",
        metricName="weightedPrecision"
    )
    precision = evaluator_precision.evaluate(predictions)
    
    evaluator_recall = MulticlassClassificationEvaluator(
        labelCol="label",
        predictionCol="prediction",
        metricName="weightedRecall"
    )
    recall = evaluator_recall.evaluate(predictions)
    
    evaluator_f1 = MulticlassClassificationEvaluator(
        labelCol="label",
        predictionCol="prediction",
        metricName="f1"
    )
    f1 = evaluator_f1.evaluate(predictions)
    
    print("\n" + "=" * 60)
    print("模型评估结果:")
    print("=" * 60)
    print(f"  AUC-ROC:     {auc:.4f}")
    print(f"  准确率:       {accuracy:.4f}")
    print(f"  精确率:       {precision:.4f}")
    print(f"  召回率:       {recall:.4f}")
    print(f"  F1分数:       {f1:.4f}")
    print("=" * 60)
    
    print("\n[7] 混淆矩阵分析...")
    tp = predictions.filter((col("label") == 1.0) & (col("prediction") == 1.0)).count()
    tn = predictions.filter((col("label") == 0.0) & (col("prediction") == 0.0)).count()
    fp = predictions.filter((col("label") == 0.0) & (col("prediction") == 1.0)).count()
    fn = predictions.filter((col("label") == 1.0) & (col("prediction") == 0.0)).count()
    
    print(f"  真阳性(TP): {tp} - 违规样本被正确识别")
    print(f"  真阴性(TN): {tn} - 不违规样本被正确识别")
    print(f"  假阳性(FP): {fp} - 不违规样本被误判为违规")
    print(f"  假阴性(FN): {fn} - 违规样本被误判为不违规")
    
    print("\n[8] 保存模型...")
    model_path = os.path.join(base_path, "lr_text_classification_model")
    model.write().overwrite().save(model_path)
    print(f"  - 模型已保存至: {model_path}")
    
    print("\n[9] 模型训练完成，可使用保存的模型进行预测。")
    
    spark.stop()
    print("\n训练完成!")

if __name__ == "__main__":
    main()
