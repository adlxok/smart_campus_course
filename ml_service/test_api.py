# -*- coding: utf-8 -*-
import requests
import json

BASE_URL = "http://127.0.0.1:5001"

def test_single_predict(text):
    print(f"\n{'='*60}")
    print(f"测试文本: {text}")
    print("="*60)
    
    response = requests.post(
        f"{BASE_URL}/api/predict",
        json={"text": text},
        headers={"Content-Type": "application/json"}
    )
    
    result = response.json()
    print(f"响应状态码: {response.status_code}")
    print(f"返回结果: {json.dumps(result, ensure_ascii=False, indent=2)}")
    return result

def test_batch_predict(texts):
    print(f"\n{'='*60}")
    print(f"批量测试 {len(texts)} 条文本")
    print("="*60)
    
    response = requests.post(
        f"{BASE_URL}/api/predict/batch",
        json={"texts": texts},
        headers={"Content-Type": "application/json"}
    )
    
    result = response.json()
    print(f"响应状态码: {response.status_code}")
    print(f"返回结果: {json.dumps(result, ensure_ascii=False, indent=2)}")
    return result

def test_health():
    print(f"\n{'='*60}")
    print("健康检查")
    print("="*60)
    
    response = requests.get(f"{BASE_URL}/api/health")
    result = response.json()
    print(f"返回结果: {json.dumps(result, ensure_ascii=False, indent=2)}")
    return result

def test_model_info():
    print(f"\n{'='*60}")
    print("模型信息")
    print("="*60)
    
    response = requests.get(f"{BASE_URL}/api/model/info")
    result = response.json()
    print(f"返回结果: {json.dumps(result, ensure_ascii=False, indent=2)}")
    return result

if __name__ == "__main__":
    print("=" * 60)
    print("文本分类模型 API 测试")
    print("=" * 60)
    
    test_health()
    test_model_info()
    
    test_cases = [
        "今天天气真好，适合出去散步",
        "加我微信，带你赚钱，日入过万",
        "牛牛金花玩的加我，稳赚不赔",
        "这个视频拍得很好看，学到了很多",
        "代发短信，有想做的联系我，无押金",
        "你好，请问这个怎么使用？",
        "私聊我，带你玩彩票，包赚不赔",
        "感谢分享，很有帮助的内容",
    ]
    
    print("\n" + "=" * 60)
    print("单条文本预测测试")
    print("=" * 60)
    
    for text in test_cases:
        test_single_predict(text)
    
    print("\n" + "=" * 60)
    print("批量文本预测测试")
    print("=" * 60)
    test_batch_predict(test_cases[:4])
    
    print("\n" + "=" * 60)
    print("测试完成!")
    print("=" * 60)
