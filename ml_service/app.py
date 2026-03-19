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
import threading
import uuid
import subprocess
import tempfile
import shutil
from datetime import datetime
from playwright.async_api import async_playwright
from hdfs import InsecureClient

app = Flask(__name__)

MODEL_PATH = r"d:\A_Graduation_Project\project\p2_0\lr_text_classification_model_optimized"
VIOLATION_THRESHOLD = 0.9
HDFS_URL = "http://localhost:50070"
HDFS_NAMENODE = "hdfs://localhost:9000"

spark = None
model = None

crawler_tasks = {}
import_tasks = {}

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
    "Cookie": "buvid3=47677070-488E-5B27-0E1A-C882E333757425995infoc; b_nut=1745932225; _uuid=210F271101-DE4A-103C2-1027C-916411F510EDF26747infoc; enable_web_push=DISABLE; enable_feed_channel=ENABLE; rpdid=|(YYJ)ukkmu0J'u~RlmRukll; buvid_fp=20b4e9c1fcd93b1f9714496753d9d2bf; header_theme_version=OPEN; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; PVID=1; LIVE_BUVID=AUTO6317619205625200; CURRENT_QUALITY=80; buvid4=64CB7880-B1B2-3802-2213-F668646AB63G38993-026022815-2eVVYWuB7swYubBfyNf7XQ%3D%3D; ogv_device_support_hdr=0; ogv_device_support_dolby=0; bmg_af_switch=1; bmg_src_def_domain=i0.hdslb.com; theme-switch-show=SHOWED; csrf_state=6796c969eb7608a70a892db6c756bacc; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzQxODA0NDQsImlhdCI6MTc3MzkyMTE4NCwicGx0IjotMX0.37VjDFf9--UtybZnG4Y4ZQqNKEM8Z9P49JHol9QfeIE; bili_ticket_expires=1774180384; SESSDATA=188b2aa4%2C1789473273%2C67e81%2A32CjBD2BPou1OKVe4PWSetKuDvfl7E6ChG7DTI-rpYHhwzHe8y_7gU78Oxor-yim94cyQSVkVsRWRvYlp0eXFmSGN1YUlGbjFEbkxoQllKaFkwcWgtR0tQRk02ZXZ4WTZ5cFNoUkotNG9uR1kySENLclBtemNLMlRTN0F0Qk1zWWFCallGVzVYZVNRIIEC; bili_jct=a7912d1a085208d1b8aa8dd2e0131074; DedeUserID=3546624886311378; DedeUserID__ckMd5=98c8a3d86ab111b0; sid=4y11c1m6; home_feed_column=4; browser_resolution=1352-911; CURRENT_FNVAL=4048; bp_t_offset_3546624886311378=1181472441440927744; b_lsid=C6F25DA9_19D05F46C88"
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

class CrawlerTask:
    def __init__(self, task_id, url, max_videos):
        self.task_id = task_id
        self.url = url
        self.max_videos = max_videos
        self.status = "pending"
        self.progress = "等待执行..."
        self.logs = []
        self.result = None
        self.created_at = datetime.now()
    
    def add_log(self, message):
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.logs.append(f"[{timestamp}] {message}")
        if len(self.logs) > 1000:
            self.logs = self.logs[-500:]
    
    def to_dict(self):
        return {
            'taskId': self.task_id,
            'url': self.url,
            'maxVideos': self.max_videos,
            'status': self.status,
            'progress': self.progress,
            'logs': self.logs,
            'result': self.result,
            'createdAt': self.created_at.isoformat()
        }

class ImportTask:
    def __init__(self, task_id, video_ids=None, import_all=False, category=None, limit=None, user_id=None):
        self.task_id = task_id
        self.video_ids = video_ids or []
        self.import_all = import_all
        self.category = category
        self.limit = limit
        self.user_id = user_id
        self.status = "pending"
        self.progress = "等待执行..."
        self.logs = []
        self.result = None
        self.created_at = datetime.now()
        self.total_count = 0
        self.success_count = 0
        self.fail_count = 0
        self.skip_count = 0
    
    def add_log(self, message):
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.logs.append(f"[{timestamp}] {message}")
        if len(self.logs) > 1000:
            self.logs = self.logs[-500:]
    
    def to_dict(self):
        return {
            'taskId': self.task_id,
            'videoIds': self.video_ids,
            'importAll': self.import_all,
            'category': self.category,
            'limit': self.limit,
            'userId': self.user_id,
            'status': self.status,
            'progress': self.progress,
            'logs': self.logs,
            'result': self.result,
            'totalCount': self.total_count,
            'successCount': self.success_count,
            'failCount': self.fail_count,
            'skipCount': self.skip_count,
            'createdAt': self.created_at.isoformat()
        }

def get_hdfs_client():
    try:
        client = InsecureClient(HDFS_URL, user='root')
        return client
    except Exception as e:
        print(f"HDFS连接失败: {e}")
        return None

def ensure_hdfs_dirs(client):
    dirs = ['/videos', '/covers']
    for d in dirs:
        try:
            if not client.status(d, strict=False):
                client.makedirs(d)
        except:
            client.makedirs(d)

def download_file(url, filepath, headers_dict, task=None):
    try:
        response = requests.get(url, headers=headers_dict, stream=True, timeout=60)
        total_size = int(response.headers.get('content-length', 0))
        downloaded = 0
        
        with open(filepath, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                if chunk:
                    f.write(chunk)
                    downloaded += len(chunk)
        
        return True
    except Exception as e:
        if task:
            task.add_log(f"下载失败: {e}")
        return False

def merge_video_audio(video_path, audio_path, output_path, task=None):
    cmd = [
        'ffmpeg',
        '-i', video_path,
        '-i', audio_path,
        '-c:v', 'copy',
        '-c:a', 'aac',
        '-y',
        output_path
    ]
    
    try:
        subprocess.run(cmd, check=True, capture_output=True, timeout=300)
        return True
    except subprocess.CalledProcessError as e:
        if task:
            task.add_log(f"合并失败: {e}")
        return False
    except FileNotFoundError:
        if task:
            task.add_log("错误: 未找到ffmpeg，请确保已安装ffmpeg并添加到系统PATH")
        return False
    except subprocess.TimeoutExpired:
        if task:
            task.add_log("错误: 合并超时")
        return False

def upload_to_hdfs(local_path, hdfs_path, task=None):
    try:
        client = get_hdfs_client()
        if not client:
            return None
        
        ensure_hdfs_dirs(client)
        client.upload(hdfs_path, local_path, overwrite=True)
        return f"{HDFS_NAMENODE}{hdfs_path}"
    except Exception as e:
        if task:
            task.add_log(f"HDFS上传失败: {e}")
        return None

def get_or_create_tag(tag_name, conn):
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM tag WHERE name = %s", (tag_name,))
    result = cursor.fetchone()
    if result:
        return result[0]
    
    cursor.execute("INSERT INTO tag (name) VALUES (%s)", (tag_name,))
    conn.commit()
    return cursor.lastrowid

def get_category_id_by_name(category_name, conn):
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM category WHERE name = %s", (category_name,))
    result = cursor.fetchone()
    return result[0] if result else None

def check_video_exists(bvid, conn):
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM video WHERE bvid = %s", (bvid,))
    return cursor.fetchone() is not None

def import_single_video(video_data, task, videos_dir, covers_dir):
    bvid = video_data['bvid']
    
    conn = pymysql.connect(**db_config)
    try:
        if check_video_exists(bvid, conn):
            task.add_log(f"[跳过] BV号 {bvid} 已存在")
            task.skip_count += 1
            return False
        
        task.add_log(f"[处理] {bvid} - {video_data['title'][:30]}...")
        
        video_url = video_data.get('video_url', '')
        audio_url = video_data.get('audio_url', '')
        cover_url = video_data.get('cover', '')
        
        hdfs_video_path = None
        hdfs_cover_path = None
        
        if video_url and audio_url:
            task.add_log(f"  下载视频流到本地...")
            video_file = os.path.join(videos_dir, f"{bvid}_video.m4s")
            audio_file = os.path.join(videos_dir, f"{bvid}_audio.m4s")
            merged_file = os.path.join(videos_dir, f"{bvid}.mp4")
            
            if download_file(video_url, video_file, headers, task):
                if download_file(audio_url, audio_file, headers, task):
                    task.add_log(f"  合并音视频...")
                    if merge_video_audio(video_file, audio_file, merged_file, task):
                        hdfs_video_path = f"/videos/{bvid}.mp4"
                        hdfs_video_url = upload_to_hdfs(merged_file, hdfs_video_path, task)
                        if hdfs_video_url:
                            task.add_log(f"  视频已上传到HDFS: {hdfs_video_path}")
                        else:
                            hdfs_video_path = None
                    
                    if os.path.exists(video_file):
                        os.remove(video_file)
                    if os.path.exists(audio_file):
                        os.remove(audio_file)
        
        if cover_url:
            task.add_log(f"  下载封面到本地...")
            cover_file = os.path.join(covers_dir, f"{bvid}.jpg")
            if download_file(cover_url, cover_file, headers, task):
                hdfs_cover_path = f"/covers/{bvid}.jpg"
                hdfs_cover_url = upload_to_hdfs(cover_file, hdfs_cover_path, task)
                if hdfs_cover_url:
                    task.add_log(f"  封面已上传到HDFS: {hdfs_cover_path}")
                else:
                    hdfs_cover_path = None
        
        category_id = None
        if video_data.get('category'):
            category_id = get_category_id_by_name(video_data['category'], conn)
        
        cursor = conn.cursor()
        sql = """
        INSERT INTO video (bvid, title, description, video_url, cover_url, user_id, username, view_count, category_id)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        video_description = f"播放量: {video_data.get('view_count', 0)}, 点赞: {video_data.get('like_count', 0)}"
        
        if hdfs_video_path:
            final_video_url = f"{HDFS_NAMENODE}{hdfs_video_path}"
        else:
            final_video_url = video_url
        
        if hdfs_cover_path:
            final_cover_url = f"{HDFS_NAMENODE}{hdfs_cover_path}"
        else:
            final_cover_url = cover_url
        
        user_id = task.user_id if task.user_id else 8
        username_val = 'admin'
        
        cursor.execute(sql, (
            bvid,
            video_data['title'],
            video_description,
            final_video_url,
            final_cover_url,
            user_id,
            username_val,
            video_data.get('view_count', 0),
            category_id
        ))
        video_id = cursor.lastrowid
        conn.commit()
        
        tags_json = video_data.get('tags', '[]')
        try:
            if isinstance(tags_json, str):
                tags = json.loads(tags_json)
            else:
                tags = tags_json
            
            if isinstance(tags, list):
                for tag_name in tags[:5]:
                    if tag_name and tag_name.strip():
                        tag_id = get_or_create_tag(tag_name.strip(), conn)
                        cursor.execute(
                            "INSERT INTO video_tag (video_id, tag_id) VALUES (%s, %s)",
                            (video_id, tag_id)
                        )
                conn.commit()
        except Exception as e:
            task.add_log(f"  标签处理失败: {e}")
        
        task.add_log(f"  [成功] 视频ID: {video_id}")
        task.success_count += 1
        return True
        
    except Exception as e:
        task.add_log(f"  [失败] {e}")
        task.fail_count += 1
        return False
    finally:
        conn.close()

def run_import_task(task):
    task.status = "running"
    task.add_log("=" * 50)
    task.add_log("开始视频导入任务")
    if task.category:
        task.add_log(f"分类筛选: {task.category}")
    if task.limit:
        task.add_log(f"导入数量限制: {task.limit}条")
    task.add_log("=" * 50)
    
    base_dir = os.path.dirname(os.path.abspath(__file__))
    videos_dir = os.path.join(base_dir, "downloaded_videos")
    covers_dir = os.path.join(base_dir, "downloaded_covers")
    
    for d in [videos_dir, covers_dir]:
        if not os.path.exists(d):
            os.makedirs(d)
    
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        if task.video_ids:
            placeholders = ','.join(['%s'] * len(task.video_ids))
            sql = f"""
                SELECT id, bvid, title, tags, category, cover, video_url, audio_url,
                       view_count, like_count, coin_count, favorite_count, create_time
                FROM bilibili_video
                WHERE id IN ({placeholders})
            """
            cursor.execute(sql, task.video_ids)
        else:
            where_clauses = ["b.bvid NOT IN (SELECT bvid FROM video)"]
            params = []
            
            if task.category:
                where_clauses.append("b.category = %s")
                params.append(task.category)
            
            where_sql = " AND ".join(where_clauses)
            
            limit_sql = ""
            if task.limit:
                limit_sql = f"LIMIT {int(task.limit)}"
            
            sql = f"""
                SELECT b.id, b.bvid, b.title, b.tags, b.category, b.cover, b.video_url, b.audio_url,
                       b.view_count, b.like_count, b.coin_count, b.favorite_count, b.create_time
                FROM bilibili_video b
                WHERE {where_sql}
                ORDER BY b.create_time DESC
                {limit_sql}
            """
            cursor.execute(sql, params)
        
        videos = cursor.fetchall()
        conn.close()
        
        task.total_count = len(videos)
        task.add_log(f"共找到 {task.total_count} 个视频待处理（已排除重复）")
        
        for i, row in enumerate(videos, 1):
            task.progress = f"处理中 {i}/{task.total_count}"
            video_data = {
                'id': row[0],
                'bvid': row[1],
                'title': row[2],
                'tags': row[3],
                'category': row[4],
                'cover': row[5],
                'video_url': row[6],
                'audio_url': row[7],
                'view_count': row[8],
                'like_count': row[9],
                'coin_count': row[10],
                'favorite_count': row[11],
                'create_time': row[12]
            }
            
            import_single_video(video_data, task, videos_dir, covers_dir)
        
        task.status = "completed"
        task.progress = "导入完成"
        task.result = {
            'total': task.total_count,
            'success': task.success_count,
            'fail': task.fail_count,
            'skip': task.skip_count
        }
        
        task.add_log("=" * 50)
        task.add_log(f"导入完成! 成功: {task.success_count}, 失败: {task.fail_count}, 跳过: {task.skip_count}")
        task.add_log("=" * 50)
        
    except Exception as e:
        task.status = "failed"
        task.progress = f"导入失败: {str(e)}"
        task.add_log(f"[错误] {str(e)}")

def save_to_mysql(video_info, task):
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
        task.add_log(f"[MySQL] 保存失败: {e}")
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

def clean_video_data(task):
    task.add_log("[4] 开始清洗数据...")
    try:
        valid_categories = get_valid_categories()
        if not valid_categories:
            task.add_log("警告: 未获取到有效分类，跳过数据清洗")
            return 0
        
        task.add_log(f"有效分类: {', '.join(valid_categories)}")
        
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
        
        task.add_log(f"数据清洗完成，删除了 {deleted_count} 条无效分类的视频")
        return deleted_count
    except Exception as e:
        task.add_log(f"[数据清洗失败] {e}")
        return 0

async def crawl_bilibili_async(task):
    task.add_log("=" * 50)
    task.add_log(f"Playwright 自动化爬取 B站 (目标: {task.max_videos}条)")
    task.add_log("=" * 50)
    
    try:
        async with async_playwright() as p:
            browser = await p.chromium.launch(headless=False)
            context = await browser.new_context()
            
            await context.add_cookies([
                {"name": "SESSDATA", "value": "ed58402d%2C1789215749%2C92fed%2A32CjDndERecaqXHhtLNa5F6EzUKBXXVM1L25WGHmpKHEaghtKmqGIUrMYJP6y1BQhEFc4SVlhmTDZLcVNHbWN1dWNsUTRhM2RPTGw2THNySFEyVnhSYU9zb0lKemtGZEJsNHdvZDJOM1lXNWpBekdid2pFQzFLeEEwUm1tdC1UU1dDRGg3NXNmcXRBIIEC", "domain": ".bilibili.com", "path": "/"}
            ])
            
            page = await context.new_page()
            
            task.add_log(f"[1] 打开B站页面: {task.url}")
            task.progress = "正在打开页面..."
            await page.goto(task.url, wait_until="networkidle")
            await page.wait_for_timeout(2000)
            
            task.add_log("[2] 滚动页面获取视频链接...")
            task.progress = "正在获取视频链接..."
            video_links = set()
            scroll_count = 0
            max_scrolls = 100
            no_new_links_count = 0
            
            while len(video_links) < task.max_videos and scroll_count < max_scrolls:
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
                    task.add_log("连续5次滚动无新链接，停止滚动")
                    break
                
                scroll_count += 1
                task.add_log(f"滚动 {scroll_count} 次，已获取 {len(video_links)} 个链接")
                task.progress = f"已获取 {len(video_links)} 个链接"
                
                await page.evaluate('window.scrollTo(0, document.body.scrollHeight)')
                await page.wait_for_timeout(1000)
            
            unique_links = list(video_links)[:task.max_videos]
            task.add_log(f"[3] 共获取 {len(unique_links)} 个视频链接，准备爬取")
            
            success_count = 0
            fail_count = 0
            
            for i, video_url in enumerate(unique_links, 1):
                task.add_log(f"[{i}/{len(unique_links)}] 爬取: {video_url}")
                task.progress = f"正在爬取 {i}/{len(unique_links)}"
                
                try:
                    html = get_html_by_requests(video_url)
                    video_info = parse_video_data(html)
                    
                    if video_info.get('bvid'):
                        title_short = video_info['title'][:40] + "..." if len(video_info['title']) > 40 else video_info['title']
                        task.add_log(f"标题: {title_short}")
                        task.add_log(f"BV号: {video_info['bvid']}")
                        task.add_log(f"播放量: {video_info['view']:,}")
                        
                        if save_to_mysql(video_info, task):
                            success_count += 1
                        else:
                            fail_count += 1
                    else:
                        task.add_log("跳过: 无法解析视频数据")
                        fail_count += 1
                        
                except Exception as e:
                    task.add_log(f"错误: {e}")
                    fail_count += 1
                
                if i % 50 == 0:
                    task.add_log(f"[进度] 已爬取 {success_count} 条，失败 {fail_count} 条")
            
            await browser.close()
            
            task.add_log("=" * 50)
            task.add_log(f"爬取完成! 成功: {success_count} 条, 失败: {fail_count} 条")
            task.add_log("=" * 50)
            
            deleted_count = clean_video_data(task)
            
            task.status = "completed"
            task.progress = "爬取完成"
            task.result = {
                'success': True,
                'total': len(unique_links),
                'successCount': success_count,
                'failCount': fail_count,
                'deletedCount': deleted_count
            }
            
    except Exception as e:
        task.status = "failed"
        task.progress = f"爬取失败: {str(e)}"
        task.add_log(f"[错误] {str(e)}")
        task.result = {
            'success': False,
            'error': str(e)
        }

def run_crawler_task(task):
    asyncio.run(crawl_bilibili_async(task))

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
        
        task_id = str(uuid.uuid4())
        task = CrawlerTask(task_id, url, max_videos)
        crawler_tasks[task_id] = task
        
        thread = threading.Thread(target=run_crawler_task, args=(task,))
        thread.daemon = True
        thread.start()
        
        return jsonify({
            'success': True,
            'taskId': task_id,
            'message': '爬虫任务已启动'
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/crawler/status/<task_id>', methods=['GET'])
def get_crawler_status(task_id):
    task = crawler_tasks.get(task_id)
    if not task:
        return jsonify({
            'success': False,
            'error': '任务不存在'
        }), 404
    
    return jsonify({
        'success': True,
        'data': task.to_dict()
    })

@app.route('/api/crawler/logs/<task_id>', methods=['GET'])
def get_crawler_logs(task_id):
    task = crawler_tasks.get(task_id)
    if not task:
        return jsonify({
            'success': False,
            'error': '任务不存在'
        }), 404
    
    since_index = request.args.get('since', 0, type=int)
    logs = task.logs[since_index:]
    next_index = since_index + len(logs)
    
    return jsonify({
        'success': True,
        'data': {
            'taskId': task_id,
            'status': task.status,
            'progress': task.progress,
            'logs': logs,
            'totalLogs': len(task.logs),
            'nextIndex': next_index,
            'result': task.result
        }
    })

@app.route('/api/crawler/tasks', methods=['GET'])
def get_all_crawler_tasks():
    tasks = [task.to_dict() for task in crawler_tasks.values()]
    return jsonify({
        'success': True,
        'data': tasks
    })

@app.route('/api/crawler/delete/<task_id>', methods=['DELETE'])
def delete_crawler_task(task_id):
    if task_id in crawler_tasks:
        del crawler_tasks[task_id]
        return jsonify({
            'success': True,
            'message': '任务已删除'
        })
    return jsonify({
        'success': False,
        'error': '任务不存在'
    }), 404

@app.route('/api/import/start', methods=['POST'])
def start_import():
    try:
        data = request.get_json() or {}
        
        video_ids = data.get('videoIds', [])
        import_all = data.get('importAll', False)
        category = data.get('category', None)
        limit = data.get('limit', None)
        user_id = data.get('userId', None)
        
        if not video_ids and not import_all and not category and not limit:
            return jsonify({
                'success': False,
                'error': '请指定要导入的视频ID或设置筛选条件'
            }), 400
        
        task_id = str(uuid.uuid4())
        task = ImportTask(task_id, video_ids, import_all, category, limit, user_id)
        import_tasks[task_id] = task
        
        thread = threading.Thread(target=run_import_task, args=(task,))
        thread.daemon = True
        thread.start()
        
        return jsonify({
            'success': True,
            'taskId': task_id,
            'message': '导入任务已启动'
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/import/status/<task_id>', methods=['GET'])
def get_import_status(task_id):
    task = import_tasks.get(task_id)
    if not task:
        return jsonify({
            'success': False,
            'error': '任务不存在'
        }), 404
    
    return jsonify({
        'success': True,
        'data': task.to_dict()
    })

@app.route('/api/import/logs/<task_id>', methods=['GET'])
def get_import_logs(task_id):
    task = import_tasks.get(task_id)
    if not task:
        return jsonify({
            'success': False,
            'error': '任务不存在'
        }), 404
    
    since_index = request.args.get('since', 0, type=int)
    logs = task.logs[since_index:]
    next_index = since_index + len(logs)
    
    return jsonify({
        'success': True,
        'data': {
            'taskId': task_id,
            'status': task.status,
            'progress': task.progress,
            'logs': logs,
            'totalLogs': len(task.logs),
            'nextIndex': next_index,
            'result': task.result
        }
    })

@app.route('/api/import/tasks', methods=['GET'])
def get_all_import_tasks():
    tasks = [task.to_dict() for task in import_tasks.values()]
    return jsonify({
        'success': True,
        'data': tasks
    })

@app.route('/api/import/delete/<task_id>', methods=['DELETE'])
def delete_import_task(task_id):
    if task_id in import_tasks:
        del import_tasks[task_id]
        return jsonify({
            'success': True,
            'message': '任务已删除'
        })
    return jsonify({
        'success': False,
        'error': '任务不存在'
    }), 404

@app.route('/api/bilibili/videos', methods=['GET'])
def get_bilibili_videos():
    try:
        page = request.args.get('page', 1, type=int)
        page_size = request.args.get('pageSize', 20, type=int)
        keyword = request.args.get('keyword', '')
        
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        where_clause = ""
        params = []
        if keyword:
            where_clause = "WHERE title LIKE %s OR bvid LIKE %s"
            params = [f'%{keyword}%', f'%{keyword}%']
        
        count_sql = f"SELECT COUNT(*) FROM bilibili_video {where_clause}"
        cursor.execute(count_sql, params)
        total = cursor.fetchone()[0]
        
        offset = (page - 1) * page_size
        sql = f"""
            SELECT id, bvid, title, tags, category, cover, view_count, like_count, create_time
            FROM bilibili_video
            {where_clause}
            ORDER BY id DESC
            LIMIT %s OFFSET %s
        """
        cursor.execute(sql, params + [page_size, offset])
        
        videos = []
        for row in cursor.fetchall():
            videos.append({
                'id': row[0],
                'bvid': row[1],
                'title': row[2],
                'tags': row[3],
                'category': row[4],
                'cover': row[5],
                'viewCount': row[6],
                'likeCount': row[7],
                'createTime': row[8].isoformat() if row[8] else None
            })
        
        conn.close()
        
        return jsonify({
            'success': True,
            'data': videos,
            'total': total,
            'page': page,
            'pageSize': page_size
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/import/check-exists', methods=['POST'])
def check_videos_exist():
    try:
        data = request.get_json() or {}
        bvids = data.get('bvids', [])
        
        if not bvids:
            return jsonify({
                'success': True,
                'data': {'exists': [], 'notExists': []}
            })
        
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        placeholders = ','.join(['%s'] * len(bvids))
        cursor.execute(f"SELECT bvid FROM video WHERE bvid IN ({placeholders})", bvids)
        existing = [row[0] for row in cursor.fetchall()]
        
        conn.close()
        
        not_exists = [b for b in bvids if b not in existing]
        
        return jsonify({
            'success': True,
            'data': {
                'exists': existing,
                'notExists': not_exists
            }
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
    app.run(host='0.0.0.0', port=5001, debug=False, threaded=True)
