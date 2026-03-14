# -*- coding: utf-8 -*-
from flask import Flask, request, jsonify
from pyspark.sql import SparkSession
from pyspark.ml import PipelineModel
from pyspark.sql.types import StringType, StructType, StructField, ArrayType
from pyspark.sql.functions import udf, col
import os
import jieba

app = Flask(__name__)

MODEL_PATH = r"d:\A_Graduation_Project\project\p2_0\lr_text_classification_model_optimized"
VIOLATION_THRESHOLD = 0.9

spark = None
model = None

def chinese_tokenize(text):
    if text is None:
        return []
    words = jieba.cut(text)
    return [w.strip() for w in words if len(w.strip()) > 0]

def init_spark():
    global spark, model
    if spark is None:
        spark = SparkSession.builder \
            .appName("TextClassificationService") \
            .master("local[*]") \
            .config("spark.driver.memory", "2g") \
            .config("spark.sql.legacy.json.allowEmptyString.enabled", "true") \
            .getOrCreate()
        spark.sparkContext.setLogLevel("ERROR")
        
    if model is None and os.path.exists(MODEL_PATH):
        model = PipelineModel.load(MODEL_PATH)
        print("优化版模型加载成功!")
    
    return spark, model

@app.route('/api/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'error': '请提供text参数'
            }), 400
        
        text = data['text']
        
        if not text or not text.strip():
            return jsonify({
                'success': False,
                'error': '文本不能为空'
            }), 400
        
        spark_session, loaded_model = init_spark()
        
        if loaded_model is None:
            return jsonify({
                'success': False,
                'error': '模型未加载'
            }), 500
        
        tokenize_udf = udf(chinese_tokenize, ArrayType(StringType()))
        
        schema = StructType([StructField("文本", StringType(), True)])
        df = spark_session.createDataFrame([(text,)], schema)
        df = df.withColumn("words", tokenize_udf(col("文本")))
        
        prediction = loaded_model.transform(df)
        result = prediction.select("prediction", "probability").collect()[0]
        
        probability = result["probability"]
        violation_prob = float(probability[1])
        
        is_violation = violation_prob >= VIOLATION_THRESHOLD
        label_name = "违规" if is_violation else "不违规"
        
        return jsonify({
            'success': True,
            'data': {
                'text': text,
                'label': label_name,
                'isViolation': is_violation,
                'violationProbability': round(violation_prob, 4),
                'confidence': round(max(violation_prob, 1 - violation_prob), 4),
                'threshold': VIOLATION_THRESHOLD
            }
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/predict/batch', methods=['POST'])
def predict_batch():
    try:
        data = request.get_json()
        
        if not data or 'texts' not in data:
            return jsonify({
                'success': False,
                'error': '请提供texts参数(文本列表)'
            }), 400
        
        texts = data['texts']
        
        if not isinstance(texts, list) or len(texts) == 0:
            return jsonify({
                'success': False,
                'error': 'texts必须是非空列表'
            }), 400
        
        spark_session, loaded_model = init_spark()
        
        if loaded_model is None:
            return jsonify({
                'success': False,
                'error': '模型未加载'
            }), 500
        
        tokenize_udf = udf(chinese_tokenize, ArrayType(StringType()))
        
        text_data = [(text,) for text in texts]
        schema = StructType([StructField("文本", StringType(), True)])
        df = spark_session.createDataFrame(text_data, schema)
        df = df.withColumn("words", tokenize_udf(col("文本")))
        
        predictions = loaded_model.transform(df)
        results = predictions.select("文本", "prediction", "probability").collect()
        
        output = []
        for row in results:
            text = row["文本"]
            probability = row["probability"]
            violation_prob = float(probability[1])
            
            is_violation = violation_prob >= VIOLATION_THRESHOLD
            
            output.append({
                'text': text,
                'label': "违规" if is_violation else "不违规",
                'isViolation': is_violation,
                'violationProbability': round(violation_prob, 4),
                'confidence': round(max(violation_prob, 1 - violation_prob), 4),
                'threshold': VIOLATION_THRESHOLD
            })
        
        return jsonify({
            'success': True,
            'data': output,
            'total': len(output)
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({
        'status': 'healthy',
        'service': 'text-classification-service',
        'model': 'logistic-regression-optimized'
    })

@app.route('/api/model/info', methods=['GET'])
def model_info():
    return jsonify({
        'success': True,
        'data': {
            'modelName': 'Logistic Regression Text Classifier (Optimized)',
            'modelType': 'Binary Classification',
            'labels': ['不违规', '违规'],
            'features': 'TF-IDF (10000 features) + jieba中文分词',
            'violationThreshold': VIOLATION_THRESHOLD,
            'optimizations': [
                '中文分词(jieba)',
                '类别权重平衡',
                'CountVectorizer特征提取',
                f'阈值调整({VIOLATION_THRESHOLD})'
            ],
            'trainingMetrics': {
                'auc': 1.0000,
                'accuracy': 0.9985,
                'precision_neg': 0.9990,
                'recall_neg': 0.9920,
                'f1_neg': 0.9955,
                'precision_pos': 0.9984,
                'recall_pos': 0.9998,
                'f1_pos': 0.9991
            }
        }
    })

if __name__ == '__main__':
    print("正在初始化Spark和加载优化版模型...")
    init_spark()
    print("服务启动中...")
    app.run(host='0.0.0.0', port=5001, debug=False)
