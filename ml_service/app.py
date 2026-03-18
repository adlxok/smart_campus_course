# -*- coding: utf-8 -*-
from flask import Flask, request, jsonify
from pyspark.sql import SparkSession
from pyspark.ml import PipelineModel
from pyspark.sql.types import StringType, StructType, StructField, ArrayType
from pyspark.sql.functions import udf, col
import os
import jieba
import asyncio
import json
import re
import pymysql
import requests
from playwright.async_api import async_playwright

app = Flask(__name__)

MODEL_PATH = r"d:\A_Graduation_Project\project\p2_0\lr_text_classification_model_optimized"
VIOLATION_THRESHOLD = 0.9

spark = None
model = None

db_config = {
    "host": "localhost",
    "user": "root",
    "password": "root",
    "database": "smart_campus_course",
    "charset": "utf8mb4"
}

headers = {
    "Referer": "https://www.bilibili.com/",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36",
    "Cookie": "buvid3=47677070-488E-5B27-0E1A-C882E333757425995infoc; b_nut=1745932225; _uuid=210F271101-DE4A-103C2-1027C-916411F510EDF26747infoc; enable_web_push=DISABLE; enable_feed_channel=ENABLE; rpdid=|(YYJ)ukkmu0J'u~RlmRukll; DedeUserID=3546624886311378; DedeUserID__ckMd5=98c8a3d86ab111b0; buvid_fp=20b4e9c1fcd93b1f9714496753d9d2bf; header_theme_version=OPEN; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; PVID=1; LIVE_BUVID=AUTO6317619205625200; CURRENT_QUALITY=80; buvid4=64CB7880-B1B2-3802-2213-F668646AB63G38993-026022815-2eVVYWuB7swYubBfyNf7XQ%3D%3D; ogv_device_support_hdr=0; ogv_device_support_dolby=0; bmg_af_switch=1; bmg_src_def_domain=i0.hdslb.com; home_feed_column=5; browser_resolution=1920-911; SESSDATA=ed58402d%2C1789215749%2C92fed%2A32CjDndERecaqXHhtLNa5F6EzUKBXXVM1L25WGHmpKHEaghtKmqGIUrMYJP6y1BQhEFc4SVlhmTDZLcVNHbWN1dWNsUTRhM2RPTGw2THNySFEyVnhSYU9zb0lKemtGZEJsNHdvZDJOM1lXNWpBekdid2pFQzFLeEEwUm1tdC1UU1dDRGg3NXNmcXRBIIEC; bili_jct=4b1f0825219053527830efdd4b202f13; sid=5zqb93e6; bp_t_offset_3546624886311378=1180678765844365312; CURRENT_FNVAL=4048; b_lsid=9611C1B9_19CFB40CB53"
}

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

def save_to_mysql(video_info):
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        tags_json = json.dumps(video_info['tags'], ensure_ascii=False)
        sql = """
        INSERT INTO bilibili_video 
        (bvid, title, tags, category, cover, video_url, audio_url, 
         view_count, danmaku_count, like_count, coin_count, favorite_count, share_count, reply_count)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
        title = VALUES(title), tags = VALUES(tags), category = VALUES(category),
        cover = VALUES(cover), video_url = VALUES(video_url), audio_url = VALUES(audio_url),
        view_count = VALUES(view_count), danmaku_count = VALUES(danmaku_count),
        like_count = VALUES(like_count), coin_count = VALUES(coin_count),
        favorite_count = VALUES(favorite_count), share_count = VALUES(share_count),
        reply_count = VALUES(reply_count), update_time = CURRENT_TIMESTAMP
        """
        cursor.execute(sql, (
            video_info['bvid'], video_info['title'], tags_json, video_info['category'],
            video_info['cover'], video_info['video_url'], video_info['audio_url'],
            video_info['view'], video_info['danmaku'], video_info['like'],
            video_info['coin'], video_info['favorite'], video_info['share'], video_info['reply']
        ))
        conn.commit()
        cursor.close()
        conn.close()
        return True
    except Exception as e:
        print(f"  [MySQL] 保存失败: {e}")
        return False

def parse_video_data(html):
    video_info = {}
    
    keyword_match = re.search(r'<meta.*?name="keywords".*?content="(.*?)"', html)
    keyword_str = keyword_match.group(1) if keyword_match else ""
    words = keyword_str.split(",")
    clean_words = words[:-4]
    category = clean_words[-1] if clean_words else "未找到"
    
    title = re.findall(r'<title>(.*?)_哔哩哔哩_bilibili</title>', html)
    video_info['title'] = title[0] if title else "未知"
    
    playinfo_match = re.findall(r'window\.__playinfo__\s*=\s*(\{.*?\})\s*</script>', html, re.DOTALL)
    if playinfo_match:
        try:
            playinfo = json.loads(playinfo_match[0])
            video_info['video_url'] = playinfo['data']['dash']['video'][0]['baseUrl']
            video_info['audio_url'] = playinfo['data']['dash']['audio'][0]['baseUrl']
        except:
            video_info['video_url'] = ""
            video_info['audio_url'] = ""
    else:
        video_info['video_url'] = ""
        video_info['audio_url'] = ""
    
    initial_state_match = re.findall(r'window\.__INITIAL_STATE__\s*=\s*(\{.*?\});', html, re.DOTALL)
    if initial_state_match:
        try:
            initial_state = json.loads(initial_state_match[0])
            video_data = initial_state.get('videoData', {})
            stat = video_data.get('stat', {})
            
            video_info['bvid'] = video_data.get('bvid', '')
            video_info['cover'] = video_data.get('pic', '')
            
            tags_list = initial_state.get('tags', [])
            video_info['tags'] = [tag.get('tag_name', '') for tag in tags_list if tag.get('tag_name')]
            
            video_info['view'] = stat.get('view', 0)
            video_info['danmaku'] = stat.get('danmaku', 0)
            video_info['like'] = stat.get('like', 0)
            video_info['coin'] = stat.get('coin', 0)
            video_info['favorite'] = stat.get('favorite', 0)
            video_info['share'] = stat.get('share', 0)
            video_info['reply'] = stat.get('reply', 0)
            video_info['category'] = category
        except:
            video_info['bvid'] = ""
            video_info['cover'] = ""
            video_info['tags'] = []
            video_info['view'] = video_info['danmaku'] = video_info['like'] = 0
            video_info['coin'] = video_info['favorite'] = video_info['share'] = video_info['reply'] = 0
            video_info['category'] = category
    else:
        video_info['bvid'] = ""
        video_info['cover'] = ""
        video_info['tags'] = []
        video_info['view'] = video_info['danmaku'] = video_info['like'] = 0
        video_info['coin'] = video_info['favorite'] = video_info['share'] = video_info['reply'] = 0
        video_info['category'] = category
    
    return video_info

def get_html_by_requests(url):
    session = requests.Session()
    response = session.get(url=url, headers=headers, timeout=30)
    return response.text

def get_valid_categories():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute("SELECT name FROM category")
        categories = [row[0] for row in cursor.fetchall()]
        cursor.close()
        conn.close()
        return set(categories)
    except Exception as e:
        print(f"[获取分类失败] {e}")
        return set()

def clean_video_data():
    print("[4] 开始清洗数据...")
    try:
        valid_categories = get_valid_categories()
        if not valid_categories:
            print("  警告: 未获取到有效分类，跳过数据清洗")
            return 0
        
        print(f"  有效分类: {valid_categories}")
        
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("SELECT bvid, category FROM bilibili_video")
        all_videos = cursor.fetchall()
        
        deleted_count = 0
        for bvid, category in all_videos:
            if category not in valid_categories:
                cursor.execute("DELETE FROM bilibili_video WHERE bvid = %s", (bvid,))
                deleted_count += 1
        
        conn.commit()
        cursor.close()
        conn.close()
        
        print(f"  数据清洗完成，删除了 {deleted_count} 条无效分类的视频")
        return deleted_count
    except Exception as e:
        print(f"[数据清洗失败] {e}")
        return 0

async def crawl_bilibili(url, max_videos):
    print("=" * 60)
    print(f"Playwright 自动化爬取 B站 (目标: {max_videos}条)")
    print("=" * 60)
    
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        
        await context.add_cookies([
            {"name": "SESSDATA", "value": "ed58402d%2C1789215749%2C92fed%2A32CjDndERecaqXHhtLNa5F6EzUKBXXVM1L25WGHmpKHEaghtKmqGIUrMYJP6y1BQhEFc4SVlhmTDZLcVNHbWN1dWNsUTRhM2RPTGw2THNySFEyVnhSYU9zb0lKemtGZEJsNHdvZDJOM1lXNWpBekdid2pFQzFLeEEwUm1tdC1UU1dDRGg3NXNmcXRBIIEC", "domain": ".bilibili.com", "path": "/"}
        ])
        
        page = await context.new_page()
        
        print(f"[1] 打开B站页面: {url}")
        await page.goto(url, wait_until="networkidle")
        await page.wait_for_timeout(2000)
        
        print("[2] 滚动页面获取视频链接...")
        video_links = set()
        scroll_count = 0
        max_scrolls = 100
        no_new_links_count = 0
        
        while len(video_links) < max_videos and scroll_count < max_scrolls:
            links = await page.eval_on_selector_all('a[href*="/video/BV"]', 'els => els.map(el => el.href)')
            prev_count = len(video_links)
            
            for link in links:
                video_links.add(link)
            
            new_count = len(video_links) - prev_count
            
            if new_count == 0:
                no_new_links_count += 1
            else:
                no_new_links_count = 0
            
            if no_new_links_count >= 5:
                print(f"连续5次滚动无新链接，停止滚动")
                break
            
            scroll_count += 1
            print(f"滚动 {scroll_count} 次，已获取 {len(video_links)} 个链接")
            
            await page.evaluate('window.scrollTo(0, document.body.scrollHeight)')
            await page.wait_for_timeout(1000)
        
        unique_links = list(video_links)[:max_videos]
        print(f"[3] 共获取 {len(unique_links)} 个视频链接，准备爬取")
        
        success_count = 0
        fail_count = 0
        
        for i, video_url in enumerate(unique_links, 1):
            print(f"[{i}/{len(unique_links)}] 爬取: {video_url}")
            
            try:
                html = get_html_by_requests(video_url)
                video_info = parse_video_data(html)
                
                if video_info.get('bvid'):
                    print(f"  标题: {video_info['title'][:40]}...")
                    print(f"  BV号: {video_info['bvid']}")
                    print(f"  播放量: {video_info['view']:,}")
                    
                    if save_to_mysql(video_info):
                        success_count += 1
                    else:
                        fail_count += 1
                else:
                    print(f"  跳过: 无法解析视频数据")
                    fail_count += 1
                    
            except Exception as e:
                print(f"  错误: {e}")
                fail_count += 1
            
            if i % 50 == 0:
                print(f"[进度] 已爬取 {success_count} 条，失败 {fail_count} 条")
        
        await browser.close()
        
        print("=" * 60)
        print(f"爬取完成! 成功: {success_count} 条, 失败: {fail_count} 条")
        print("=" * 60)
        
        deleted_count = clean_video_data()
        
        return {
            'success': True,
            'total': len(unique_links),
            'successCount': success_count,
            'failCount': fail_count,
            'deletedCount': deleted_count
        }

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

@app.route('/api/crawler/start', methods=['POST'])
def start_crawler():
    try:
        data = request.get_json()
        
        if not data or 'url' not in data:
            return jsonify({
                'success': False,
                'error': '请提供url参数'
            }), 400
        
        url = data['url']
        max_videos = data.get('maxVideos', 100)
        
        if not url or not url.strip():
            return jsonify({
                'success': False,
                'error': 'URL不能为空'
            }), 400
        
        result = asyncio.run(crawl_bilibili(url, max_videos))
        
        return jsonify({
            'success': True,
            'data': result
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
        'service': 'text-classification-and-crawler-service',
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
        }
    })

if __name__ == '__main__':
    print("正在初始化Spark和加载优化版模型...")
    init_spark()
    print("服务启动中...")
    app.run(host='0.0.0.0', port=5001, debug=False)
