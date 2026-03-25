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
import random
import time
import urllib3
from datetime import datetime
from playwright.async_api import async_playwright
from hdfs import InsecureClient

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

app = Flask(__name__)

MODEL_PATH = r"d:\A_Graduation_Project\project\p2_0\lr_text_classification_model_optimized"
VIOLATION_THRESHOLD = 0.9
HDFS_URL = "http://localhost:50070"
HDFS_NAMENODE = "hdfs://localhost:9000"

PROXY_FILE = r"d:\A_Graduation_Project\project\p2_0\IP代理.txt"
proxy_list = []

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

USER_AGENTS = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15'
]

def get_random_user_agent():
    return random.choice(USER_AGENTS)

def get_random_delay():
    return random.uniform(1, 3)

headers = {
    "Referer": "https://www.bilibili.com/",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36",
    "Cookie": "buvid3=47677070-488E-5B27-0E1A-C882E333757425995infoc; b_nut=1745932225; _uuid=210F271101-DE4A-103C2-1027C-916411F510EDF26747infoc; enable_web_push=DISABLE; enable_feed_channel=ENABLE; rpdid=|(YYJ)ukkmu0J'u~RlmRukll; buvid_fp=20b4e9c1fcd93b1f9714496753d9d2bf; header_theme_version=OPEN; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; PVID=1; LIVE_BUVID=AUTO6317619205625200; CURRENT_QUALITY=80; buvid4=64CB7880-B1B2-3802-2213-F668646AB63G38993-026022815-2eVVYWuB7swYubBfyNf7XQ%3D%3D; ogv_device_support_hdr=0; ogv_device_support_dolby=0; theme-switch-show=SHOWED; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzQxODA0NDQsImlhdCI6MTc3MzkyMTE4NCwicGx0IjotMX0.37VjDFf9--UtybZnG4Y4ZQqNKEM8Z9P49JHol9QfeIE; bili_ticket_expires=1774180384; SESSDATA=188b2aa4%2C1789473273%2C67e81%2A32CjBD2BPou1OKVe4PWSetKuDvfl7E6ChG7DTI-rpYHhwzHe8y_7gU78Oxor-yim94cyQSVkVsRWRvYlp0eXFmSGN1YUlGbjFEbkxoQllKaFkwcWgtR0tQRk02ZXZ4WTZ5cFNoUkotNG9uR1kySENLclBtemNLMlRTN0F0Qk1zWWFCallGVzVYZVNRIIEC; bili_jct=a7912d1a085208d1b8aa8dd2e0131074; DedeUserID=3546624886311378; DedeUserID__ckMd5=98c8a3d86ab111b0; sid=4y11c1m6; home_feed_column=5; browser_resolution=1920-911; bmg_af_switch=1; bmg_src_def_domain=i0.hdslb.com; bp_t_offset_3546624886311378=1181505680192831488; CURRENT_FNVAL=4048; b_lsid=7E48FCF0_19D067FA25B"
}

def get_video_info_by_api(bvid, session=None):
    try:
        request_headers = headers.copy()
        request_headers['User-Agent'] = get_random_user_agent()
        
        url = f"https://api.bilibili.com/x/web-interface/view?bvid={bvid}"
        
        if session is None:
            session = requests.Session()
        
        response = session.get(url, headers=request_headers, timeout=30)
        data = response.json()
        
        if data.get('code') != 0:
            return None, f"API错误: {data.get('message', '未知错误')}"
        
        video_data = data.get('data', {})
        
        info = {
            'bvid': video_data.get('bvid', bvid),
            'title': video_data.get('title', ''),
            'cover': video_data.get('pic', ''),
            'cid': video_data.get('cid', 0),
            'duration': video_data.get('duration', 0),
            'view': video_data.get('stat', {}).get('view', 0),
            'danmaku': video_data.get('stat', {}).get('danmaku', 0),
            'like': video_data.get('stat', {}).get('like', 0),
            'coin': video_data.get('stat', {}).get('coin', 0),
            'favorite': video_data.get('stat', {}).get('favorite', 0),
            'share': video_data.get('stat', {}).get('share', 0),
            'reply': video_data.get('stat', {}).get('reply', 0),
            'owner': video_data.get('owner', {}).get('name', ''),
            'tid': video_data.get('tid', 0),
            'tname': video_data.get('tname', ''),
        }
        
        return info, None
    except Exception as e:
        return None, str(e)

def get_play_url_by_api(bvid, cid, session=None):
    try:
        request_headers = headers.copy()
        request_headers['User-Agent'] = get_random_user_agent()
        
        url = f"https://api.bilibili.com/x/player/playurl?bvid={bvid}&cid={cid}&fnval=4048&fnver=0&fourk=1"
        
        if session is None:
            session = requests.Session()
        
        response = session.get(url, headers=request_headers, timeout=30)
        data = response.json()
        
        if data.get('code') != 0:
            return None, None, f"API错误: {data.get('message', '未知错误')}"
        
        play_data = data.get('data', {})
        dash = play_data.get('dash', {})
        
        video_url = None
        audio_url = None
        
        if dash:
            video_list = dash.get('video', [])
            audio_list = dash.get('audio', [])
            
            if video_list:
                video_url = video_list[0].get('baseUrl') or video_list[0].get('base_url')
            
            if audio_list:
                audio_url = audio_list[0].get('baseUrl') or audio_list[0].get('base_url')
        
        return video_url, audio_url, None
    except Exception as e:
        return None, None, str(e)

async def download_video_via_hellotik(bvid, title, task=None, download_dir=None):
    try:
        video_url = f"https://www.bilibili.com/video/{bvid}"
        
        if download_dir is None:
            base_dir = os.path.dirname(os.path.abspath(__file__))
            download_dir = os.path.join(base_dir, "downloaded_videos")
        
        if not os.path.exists(download_dir):
            os.makedirs(download_dir)
        
        expected_filename = f"{bvid}.mp4"
        expected_filepath = os.path.join(download_dir, expected_filename)
        
        if task:
            task.add_log(f"  使用hellotik下载视频...")
            task.add_log(f"  视频链接: {video_url}")
            task.add_log(f"  目标文件: {expected_filepath}")
        
        async with async_playwright() as p:
            browser = await p.chromium.launch(headless=True)
            context = await browser.new_context(
                user_agent=get_random_user_agent(),
                accept_downloads=True
            )
            page = await context.new_page()
            
            await page.goto("https://www.hellotik.app/zh/bilibili", wait_until="networkidle")
            await page.wait_for_timeout(2000)
            
            input_element = await page.wait_for_selector('input[type="text"]', timeout=10000)
            
            await input_element.fill(video_url)
            await page.wait_for_timeout(500)
            
            parse_button = await page.wait_for_selector('button:has-text("解析视频")', timeout=10000)
            await parse_button.click()
            
            await page.wait_for_timeout(3000)
            
            download_button = await page.wait_for_selector('button:has-text("下载无水印视频")', timeout=60000)
            
            async with page.expect_download(timeout=300000) as download_info:
                await download_button.click()
            
            download = await download_info.value
            temp_path = await download.path()
            
            shutil.copy(temp_path, expected_filepath)
            
            await browser.close()
            
            if task:
                task.add_log(f"  视频已下载到: {expected_filepath}")
            
            return expected_filepath
            
    except Exception as e:
        if task:
            task.add_log(f"  hellotik下载失败: {e}")
        return None

def download_video_via_hellotik_sync(bvid, title, task=None, download_dir=None):
    import asyncio
    try:
        loop = asyncio.get_event_loop()
    except RuntimeError:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
    
    return loop.run_until_complete(download_video_via_hellotik(bvid, title, task, download_dir))

def get_proxies_from_db():
    proxies = []
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute("SELECT protocol, host, port, username, password FROM proxy WHERE status = 1")
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        
        for row in rows:
            protocol, host, port, username, password = row
            if username and password:
                proxy_url = f"{protocol}://{username}:{password}@{host}:{port}"
            else:
                proxy_url = f"{protocol}://{host}:{port}"
            proxies.append({'http': proxy_url, 'https': proxy_url})
    except Exception as e:
        print(f"从数据库读取代理失败: {e}")
    return proxies

def get_proxies_from_file():
    proxies = []
    try:
        with open(PROXY_FILE, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            for line in lines:
                line = line.strip()
                if not line:
                    continue
                
                if line.startswith('socks5://') or line.startswith('socks4://') or line.startswith('http://') or line.startswith('https://'):
                    proxies.append({'http': line, 'https': line})
                elif ':' in line:
                    parts = line.split(':')
                    if len(parts) >= 2:
                        ip = parts[0].strip()
                        port = parts[1].strip()
                        proxies.append({'http': f'http://{ip}:{port}', 'https': f'http://{ip}:{port}'})
    except FileNotFoundError:
        print(f"警告: 未找到代理文件 {PROXY_FILE}")
    return proxies

def get_random_proxy():
    proxies = get_proxies_from_db()
    if not proxies:
        proxies = get_proxies_from_file()
    if not proxies:
        return None
    return random.choice(proxies)

def load_proxies():
    proxies = get_proxies_from_db()
    if not proxies:
        proxies = get_proxies_from_file()
    print(f"当前可用代理数: {len(proxies)}")
    return len(proxies)

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
    def __init__(self, task_id, url, max_videos, category_code=None):
        self.task_id = task_id
        self.url = url
        self.max_videos = max_videos
        self.category_code = category_code
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
            'categoryCode': self.category_code,
            'status': self.status,
            'progress': self.progress,
            'logs': self.logs,
            'result': self.result,
            'createdAt': self.created_at.isoformat()
        }

class ImportTask:
    def __init__(self, task_id, video_ids=None, import_all=False, category=None, limit=None, user_id=None, use_proxy=True):
        self.task_id = task_id
        self.video_ids = video_ids or []
        self.import_all = import_all
        self.category = category
        self.limit = limit
        self.user_id = user_id
        self.use_proxy = use_proxy
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
            'useProxy': self.use_proxy,
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

def download_file(url, filepath, headers_dict, task=None, max_retries=3, delay=3, use_proxy=True, session=None):
    for attempt in range(max_retries):
        proxy = get_random_proxy() if use_proxy else None
        proxy_str = f"使用代理: {proxy['http']}" if proxy else "不使用代理"
        if task:
            task.add_log(f"  第 {attempt + 1} 次尝试下载 ({proxy_str})")
        
        request_headers = headers_dict.copy()
        request_headers['User-Agent'] = get_random_user_agent()
        
        try:
            if session is None:
                session = requests.Session()
            session.verify = False
            
            response = session.get(
                url, 
                headers=request_headers, 
                stream=True, 
                proxies=proxy, 
                timeout=30,
                verify=False
            )
            
            if response.status_code == 200:
                total_size = int(response.headers.get('content-length', 0))
                downloaded = 0
                
                with open(filepath, 'wb') as f:
                    for chunk in response.iter_content(chunk_size=8192):
                        if chunk:
                            f.write(chunk)
                            downloaded += len(chunk)
                
                time.sleep(3)
                
                return True
            else:
                if task:
                    task.add_log(f"  下载失败，状态码: {response.status_code}")
                
        except requests.exceptions.SSLError as e:
            if task:
                task.add_log(f"  SSL错误，尝试切换代理...")
            time.sleep(1 + get_random_delay())
            continue
        except requests.exceptions.ProxyError as e:
            if task:
                task.add_log(f"  代理连接失败，尝试切换代理...")
            time.sleep(1 + get_random_delay())
            continue
        except requests.exceptions.Timeout:
            if task:
                task.add_log(f"  请求超时，尝试重新连接...")
            time.sleep(1 + get_random_delay())
            continue
        except requests.exceptions.RequestException as e:
            if task:
                task.add_log(f"  下载出错: {e}")
            time.sleep(1 + get_random_delay())
            continue
    
    if task:
        task.add_log(f"  下载失败，已达到最大重试次数")
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

def import_single_video(video_data, task, videos_dir, covers_dir, session=None):
    bvid = video_data['bvid']
    title = video_data['title']
    
    conn = pymysql.connect(**db_config)
    try:
        if check_video_exists(bvid, conn):
            task.add_log(f"[跳过] BV号 {bvid} 已存在")
            task.skip_count += 1
            return False
        
        task.add_log(f"[处理] {bvid} - {title[:30]}...")
        
        cover_url = video_data.get('cover', '')
        
        task.add_log(f"  通过API获取视频信息...")
        video_info, err = get_video_info_by_api(bvid, session)
        if err:
            task.add_log(f"  获取视频信息失败: {err}")
            task.fail_count += 1
            return False
        task.add_log(f"  通过API获取视频信息成功,{video_info}")
        if not cover_url:
            cover_url = video_info.get('cover', '')
        
        hdfs_video_path = None
        hdfs_cover_path = None
        
        downloaded_file = download_video_via_hellotik_sync(bvid, title, task)
        
        if downloaded_file and os.path.exists(downloaded_file):
            hdfs_video_path = f"/videos/{bvid}.mp4"
            hdfs_video_url = upload_to_hdfs(downloaded_file, hdfs_video_path, task)
            if hdfs_video_url:
                task.add_log(f"  视频已上传到HDFS: {hdfs_video_path}")
                try:
                    os.remove(downloaded_file)
                    task.add_log(f"  已删除本地视频文件: {downloaded_file}")
                except Exception as e:
                    task.add_log(f"  删除本地视频文件失败: {e}")
            else:
                hdfs_video_path = None
                task.add_log(f"  HDFS上传失败")
        else:
            task.add_log(f"  视频下载失败")
        
        if cover_url:
            task.add_log(f"  下载封面到本地...")
            cover_file = os.path.join(covers_dir, f"{bvid}.jpg")
            if download_file(cover_url, cover_file, headers, task, use_proxy=task.use_proxy, session=session):
                hdfs_cover_path = f"/covers/{bvid}.jpg"
                hdfs_cover_url = upload_to_hdfs(cover_file, hdfs_cover_path, task)
                if hdfs_cover_url:
                    task.add_log(f"  封面已上传到HDFS: {hdfs_cover_path}")
                    try:
                        os.remove(cover_file)
                        task.add_log(f"  已删除本地封面文件: {cover_file}")
                    except Exception as e:                                                         
                        task.add_log(f"  删除本地封面文件失败: {e}")
                else:
                    hdfs_cover_path = None
        
        category_id = None
        if video_data.get('category'):
            category_id = get_category_id_by_name(video_data['category'], conn)
        
        cursor = conn.cursor()
        sql = """
        INSERT INTO video (bvid, title, video_url, cover_url, user_id, username, category_id)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """
        
        if hdfs_video_path:
            final_video_url = hdfs_video_path
        else:
            final_video_url = video_url
        
        if hdfs_cover_path:
            final_cover_url = hdfs_cover_path
        else:
            final_cover_url = cover_url
        
        user_id = task.user_id if task.user_id else 8
        username_val = 'admin'
        
        cursor.execute(sql, (
            bvid,
            video_data['title'],
            final_video_url,
            final_cover_url,
            user_id,
            username_val,
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
    
    shared_session = requests.Session()
    shared_session.verify = False
    shared_session.headers.update({
        'User-Agent': get_random_user_agent(),
        'Referer': 'https://www.bilibili.com/'
    })
    
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
            
            import_single_video(video_data, task, videos_dir, covers_dir, session=shared_session)
        
        if task.success_count > 0:
            task.add_log("=" * 50)
            task.add_log("开始计算新导入视频的特征向量...")
            try:
                compute_features_internal()
                task.add_log("特征向量计算完成")
            except Exception as fe:
                task.add_log(f"特征计算失败: {fe}")
        
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

def get_category_map():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute("SELECT code, name FROM category WHERE code IS NOT NULL AND code != ''")
        result = cursor.fetchall()
        cursor.close()
        conn.close()
        return {row[0]: row[1] for row in result}
    except Exception as e:
        print(f"获取分类映射失败: {e}")
        return {}

def save_to_mysql(video_info, task):
    try:
        tags = video_info.get('tags', [])
        if not tags or len(tags) == 0:
            task.add_log(f"  跳过 {video_info['bvid']}: 标签为空")
            return False
        
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        tags_json = json.dumps(video_info['tags'], ensure_ascii=False)
        
        category_name = video_info['category']
        
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
            video_info['bvid'], video_info['title'], tags_json, category_name,
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
            all_tags = [tag.get('tag_name', '') for tag in tags_list if tag.get('tag_name')]
            video_info['tags'] = [tag for tag in all_tags if '发现' not in tag and '计划' not in tag]
            
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

def get_html_by_requests(url, session=None, debug=False):
    request_headers = headers.copy()
    request_headers['User-Agent'] = get_random_user_agent()
    
    if session is None:
        session = requests.Session()
    
    response = session.get(url=url, headers=request_headers, timeout=30)
    time.sleep(get_random_delay())
    
    if debug:
        return response.text, request_headers, response.status_code
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

def clean_video_tags(task):
    task.add_log("[5] 开始清洗标签数据...")
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("DELETE FROM bilibili_video WHERE tags IS NULL OR tags = '' OR tags = '[]'")
        deleted_empty = cursor.rowcount
        task.add_log(f"删除了 {deleted_empty} 条标签为空的视频")
        
        cursor.execute("SELECT id, tags FROM bilibili_video WHERE tags IS NOT NULL AND tags != ''")
        all_videos = cursor.fetchall()
        
        updated_count = 0
        for video_id, tags_str in all_videos:
            try:
                tags = json.loads(tags_str)
                if isinstance(tags, list):
                    cleaned_tags = [tag for tag in tags if '发现' not in tag and '计划' not in tag]
                    if len(cleaned_tags) != len(tags):
                        new_tags_str = json.dumps(cleaned_tags, ensure_ascii=False)
                        cursor.execute("UPDATE bilibili_video SET tags = %s WHERE id = %s", (new_tags_str, video_id))
                        updated_count += 1
            except:
                pass
        
        conn.commit()
        cursor.close()
        conn.close()
        
        task.add_log(f"标签清洗完成，删除空标签 {deleted_empty} 条，更新标签 {updated_count} 条")
        return updated_count + deleted_empty
    except Exception as e:
        task.add_log(f"[标签清洗失败] {e}")
        return 0

async def crawl_bilibili_async(task):
    task.add_log("=" * 50)
    task.add_log(f"Playwright 自动化爬取 B站 (目标: {task.max_videos}条)")
    task.add_log("=" * 50)
    
    shared_session = requests.Session()
    shared_session.verify = False
    shared_session.headers.update({
        'User-Agent': get_random_user_agent(),
        'Referer': 'https://www.bilibili.com/'
    })
    
    try:
        async with async_playwright() as p:
            browser = await p.chromium.launch(headless=False)
            context = await browser.new_context(
                user_agent=get_random_user_agent()
            )
            
            await context.add_cookies([
                {"name": "SESSDATA", "value": "188b2aa4%2C1789473273%2C67e81%2A32CjBD2BPou1OKVe4PWSetKuDvfl7E6ChG7DTI-rpYHhwzHe8y_7gU78Oxor-yim94cyQSVkVsRWRvYlp0eXFmSGN1YUlGbjFEbkxoQllKaFkwcWgtR0tQRk02ZXZ4WTZ5cFNoUkotNG9uR1kySENLclBtemNLMlRTN0F0Qk1zWWFCallGVzVYZVNRIIEC", "domain": ".bilibili.com", "path": "/"},
            ])
            
            page = await context.new_page()
            
            task.add_log(f"[1] 打开B站页面: {task.url}")
            task.progress = "正在打开页面..."
            await page.goto(task.url, wait_until="networkidle")
            await page.wait_for_timeout(int(2000 + get_random_delay() * 1000))
            
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
                await page.wait_for_timeout(int(1000 + get_random_delay() * 1000))
            
            unique_links = list(video_links)[:task.max_videos]
            task.add_log(f"[3] 共获取 {len(unique_links)} 个视频链接，准备爬取")
            
            success_count = 0
            fail_count = 0
            
            for i, video_url in enumerate(unique_links, 1):
                task.add_log(f"[{i}/{len(unique_links)}] 爬取: {video_url}")
                task.progress = f"正在爬取 {i}/{len(unique_links)}"
                
                try:
                    html, req_headers, status_code = get_html_by_requests(video_url, session=shared_session, debug=True)
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
                        task.add_log(f"  请求Headers: User-Agent={req_headers.get('User-Agent', 'N/A')}")
                        task.add_log(f"  响应状态码: {status_code}")
                        task.add_log(f"  HTML长度: {len(html)} 字符")
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
            
            clean_video_tags(task)
            
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
        category_code = data.get('categoryCode', None)
        
        if not url or not url.strip():
            return jsonify({
                'success': False,
                'error': 'URL不能为空'
            }), 400
        
        task_id = str(uuid.uuid4())
        task = CrawlerTask(task_id, url, max_videos, category_code)
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

@app.route('/api/crawler/clean-tags', methods=['POST'])
def clean_existing_tags():
    try:
        class CleanTask:
            def __init__(self):
                self.logs = []
            def add_log(self, msg):
                timestamp = datetime.now().strftime("%H:%M:%S")
                self.logs.append(f"[{timestamp}] {msg}")
                print(msg)
        
        task = CleanTask()
        updated_count = clean_video_tags(task)
        
        return jsonify({
            'success': True,
            'updatedCount': updated_count,
            'logs': task.logs
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
        use_proxy = data.get('useProxy', True)
        
        if not video_ids and not import_all and not category and not limit:
            return jsonify({
                'success': False,
                'error': '请指定要导入的视频ID或设置筛选条件'
            }), 400
        
        task_id = str(uuid.uuid4())
        task = ImportTask(task_id, video_ids, import_all, category, limit, user_id, use_proxy)
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

def get_user_favorite_tags(user_id):
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT DISTINCT t.id, t.name, COUNT(*) as weight
            FROM video_favorite vf
            JOIN video_tag vt ON vf.video_id = vt.video_id
            JOIN tag t ON vt.tag_id = t.id
            WHERE vf.user_id = %s
            GROUP BY t.id, t.name
            ORDER BY weight DESC
        """, (user_id,))
        
        tags = cursor.fetchall()
        cursor.close()
        conn.close()
        
        return [{'id': row[0], 'name': row[1], 'weight': row[2]} for row in tags]
    except Exception as e:
        print(f"获取用户收藏标签失败: {e}")
        return []

def get_all_videos_with_tags():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT v.id, v.title, v.description, v.view_count, v.cover_url, v.username,
                   GROUP_CONCAT(t.name) as tags
            FROM video v
            LEFT JOIN video_tag vt ON v.id = vt.video_id
            LEFT JOIN tag t ON vt.tag_id = t.id
            GROUP BY v.id, v.title, v.description, v.view_count, v.cover_url, v.username
        """)
        
        videos = cursor.fetchall()
        cursor.close()
        conn.close()
        
        return [{
            'id': row[0],
            'title': row[1],
            'description': row[2] or '',
            'viewCount': row[3],
            'coverUrl': row[4],
            'username': row[5],
            'tags': row[6] or ''
        } for row in videos]
    except Exception as e:
        print(f"获取视频列表失败: {e}")
        return []

def get_user_favorited_video_ids(user_id):
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("SELECT video_id FROM video_favorite WHERE user_id = %s", (user_id,))
        
        video_ids = [row[0] for row in cursor.fetchall()]
        cursor.close()
        conn.close()
        
        return set(video_ids)
    except Exception as e:
        print(f"获取用户已收藏视频失败: {e}")
        return set()

def get_video_features_from_db():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("SELECT video_id, feature_vector, tags FROM video_feature")
        
        features = {}
        for row in cursor.fetchall():
            video_id = row[0]
            feature_vector = json.loads(row[1]) if row[1] else None
            tags = row[2] or ''
            if feature_vector:
                features[video_id] = {
                    'vector': feature_vector,
                    'tags': tags
                }
        
        cursor.close()
        conn.close()
        
        return features
    except Exception as e:
        print(f"获取视频特征失败: {e}")
        return {}

def save_video_feature_to_db(video_id, feature_vector, tags):
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        feature_json = json.dumps(feature_vector.tolist())
        
        cursor.execute("""
            INSERT INTO video_feature (video_id, feature_vector, tags)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE feature_vector = VALUES(feature_vector), tags = VALUES(tags)
        """, (video_id, feature_json, tags))
        
        conn.commit()
        cursor.close()
        conn.close()
        return True
    except Exception as e:
        print(f"保存视频特征失败: {e}")
        return False

def get_video_by_id(video_id):
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT v.id, v.title, v.description, v.view_count, v.cover_url, v.username,
                   GROUP_CONCAT(t.name) as tags
            FROM video v
            LEFT JOIN video_tag vt ON v.id = vt.video_id
            LEFT JOIN tag t ON vt.tag_id = t.id
            WHERE v.id = %s
            GROUP BY v.id, v.title, v.description, v.view_count, v.cover_url, v.username
        """, (video_id,))
        
        row = cursor.fetchone()
        cursor.close()
        conn.close()
        
        if row:
            return {
                'id': row[0],
                'title': row[1],
                'description': row[2] or '',
                'viewCount': row[3],
                'coverUrl': row[4],
                'username': row[5],
                'tags': row[6] or ''
            }
        return None
    except Exception as e:
        print(f"获取视频失败: {e}")
        return None

def check_feature_exists(video_id):
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM video_feature WHERE video_id = %s", (video_id,))
        exists = cursor.fetchone() is not None
        cursor.close()
        conn.close()
        return exists
    except Exception as e:
        print(f"检查特征是否存在失败: {e}")
        return False

def compute_features_internal():
    all_videos = get_all_videos_with_tags()
    
    if not all_videos:
        return 0
    
    videos_to_compute = []
    for video in all_videos:
        if not check_feature_exists(video['id']):
            videos_to_compute.append(video)
    
    if not videos_to_compute:
        return 0
    
    video_data = []
    for video in videos_to_compute:
        content = f"{video['title']} {video['description']} {video['tags']}"
        words = chinese_tokenize(content)
        words_str = ' '.join(words)
        video_data.append((str(video['id']), words_str))
    
    spark_session, _ = init_spark()
    
    from pyspark.ml.feature import CountVectorizer, IDF
    from pyspark.sql.functions import split, col
    import numpy as np
    
    schema = StructType([
        StructField("video_id", StringType(), True),
        StructField("words_str", StringType(), True)
    ])
    df = spark_session.createDataFrame(video_data, schema)
    df = df.withColumn("words", split(col("words_str"), " "))
    
    cv = CountVectorizer(inputCol="words", outputCol="rawFeatures", vocabSize=5000)
    cv_model = cv.fit(df)
    df = cv_model.transform(df)
    
    idf = IDF(inputCol="rawFeatures", outputCol="features")
    idf_model = idf.fit(df)
    df = idf_model.transform(df)
    
    video_features = df.select("video_id", "features").collect()
    
    saved_count = 0
    for row in video_features:
        video_id = int(row["video_id"])
        features = row["features"].toArray()
        
        video_info = next((v for v in videos_to_compute if v['id'] == video_id), None)
        tags = video_info['tags'] if video_info else ''
        
        if save_video_feature_to_db(video_id, features, tags):
            saved_count += 1
    
    return saved_count

@app.route('/api/features/compute', methods=['POST'])
def compute_features():
    try:
        all_videos = get_all_videos_with_tags()
        
        if not all_videos:
            return jsonify({
                'success': True,
                'message': '没有视频需要计算特征'
            })
        
        videos_to_compute = []
        for video in all_videos:
            if not check_feature_exists(video['id']):
                videos_to_compute.append(video)
        
        if not videos_to_compute:
            return jsonify({
                'success': True,
                'message': '所有视频特征已存在，无需重新计算',
                'total': len(all_videos),
                'saved': 0
            })
        
        video_data = []
        for video in videos_to_compute:
            content = f"{video['title']} {video['description']} {video['tags']}"
            words = chinese_tokenize(content)
            words_str = ' '.join(words)
            video_data.append((str(video['id']), words_str))
        
        spark_session, _ = init_spark()
        
        from pyspark.ml.feature import CountVectorizer, IDF
        from pyspark.sql.functions import split, col
        import numpy as np
        
        schema = StructType([
            StructField("video_id", StringType(), True),
            StructField("words_str", StringType(), True)
        ])
        df = spark_session.createDataFrame(video_data, schema)
        df = df.withColumn("words", split(col("words_str"), " "))
        
        cv = CountVectorizer(inputCol="words", outputCol="rawFeatures", vocabSize=5000)
        cv_model = cv.fit(df)
        df = cv_model.transform(df)
        
        idf = IDF(inputCol="rawFeatures", outputCol="features")
        idf_model = idf.fit(df)
        df = idf_model.transform(df)
        
        video_features = df.select("video_id", "features").collect()
        
        saved_count = 0
        for row in video_features:
            video_id = int(row["video_id"])
            features = row["features"].toArray()
            
            video_info = next((v for v in videos_to_compute if v['id'] == video_id), None)
            tags = video_info['tags'] if video_info else ''
            
            if save_video_feature_to_db(video_id, features, tags):
                saved_count += 1
        
        return jsonify({
            'success': True,
            'message': f'成功计算并保存了 {saved_count} 个新视频特征',
            'total': len(all_videos),
            'newCount': len(videos_to_compute),
            'saved': saved_count
        })
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/features/compute/<int:video_id>', methods=['POST'])
def compute_single_feature(video_id):
    try:
        video = get_video_by_id(video_id)
        
        if not video:
            return jsonify({
                'success': False,
                'error': f'视频 {video_id} 不存在'
            }), 404
        
        content = f"{video['title']} {video['description']} {video['tags']}"
        words = chinese_tokenize(content)
        words_str = ' '.join(words)
        
        spark_session, _ = init_spark()
        
        from pyspark.ml.feature import CountVectorizer, IDF
        from pyspark.sql.functions import split, col
        import numpy as np
        
        schema = StructType([
            StructField("video_id", StringType(), True),
            StructField("words_str", StringType(), True)
        ])
        df = spark_session.createDataFrame([(str(video_id), words_str)], schema)
        df = df.withColumn("words", split(col("words_str"), " "))
        
        cv = CountVectorizer(inputCol="words", outputCol="rawFeatures", vocabSize=5000)
        cv_model = cv.fit(df)
        df = cv_model.transform(df)
        
        idf = IDF(inputCol="rawFeatures", outputCol="features")
        idf_model = idf.fit(df)
        df = idf_model.transform(df)
        
        result = df.select("features").collect()[0]
        features = result["features"].toArray()
        
        if save_video_feature_to_db(video_id, features, video['tags']):
            return jsonify({
                'success': True,
                'message': f'视频 {video_id} 特征计算成功',
                'videoId': video_id
            })
        else:
            return jsonify({
                'success': False,
                'error': '保存特征失败'
            }), 500
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/recommend', methods=['GET'])
def recommend_videos():
    try:
        user_id = request.args.get('userId', type=int)
        limit = request.args.get('limit', 10, type=int)
        
        if not user_id:
            return jsonify({
                'success': False,
                'error': '请提供userId参数'
            }), 400
        
        user_tags = get_user_favorite_tags(user_id)
        
        if not user_tags:
            return jsonify({
                'success': True,
                'data': [],
                'message': '用户暂无收藏记录，无法生成个性化推荐'
            })
        
        video_features = get_video_features_from_db()
        
        if not video_features:
            return jsonify({
                'success': True,
                'data': [],
                'message': '视频特征尚未计算，请先运行特征计算'
            })
        
        favorited_ids = get_user_favorited_video_ids(user_id)
        
        import numpy as np
        
        tag_names = [tag['name'] for tag in user_tags]
        tag_weights = [tag['weight'] for tag in user_tags]
        
        all_tags = set()
        for vf in video_features.values():
            if vf['tags']:
                for tag in vf['tags'].split(','):
                    all_tags.add(tag.strip())
        
        tag_to_idx = {tag: idx for idx, tag in enumerate(all_tags)}
        vocab_size = len(all_tags)
        
        user_vector = np.zeros(vocab_size)
        for tag_name, weight in zip(tag_names, tag_weights):
            if tag_name in tag_to_idx:
                user_vector[tag_to_idx[tag_name]] = weight
        user_vector = user_vector / (np.linalg.norm(user_vector) + 1e-10)
        
        scores = []
        for video_id, vf in video_features.items():
            if video_id in favorited_ids:
                continue
            
            feature_vector = np.array(vf['vector'])
            norm = np.linalg.norm(feature_vector)
            if norm > 0:
                feature_vector = feature_vector / norm
                
                video_tag_vector = np.zeros(vocab_size)
                if vf['tags']:
                    for tag in vf['tags'].split(','):
                        tag = tag.strip()
                        if tag in tag_to_idx:
                            video_tag_vector[tag_to_idx[tag]] = 1
                video_tag_vector = video_tag_vector / (np.linalg.norm(video_tag_vector) + 1e-10)
                
                similarity = np.dot(user_vector, video_tag_vector)
                scores.append((video_id, float(similarity)))
        
        scores.sort(key=lambda x: x[1], reverse=True)
        top_video_ids = [s[0] for s in scores[:limit]]
        
        all_videos = get_all_videos_with_tags()
        video_map = {v['id']: v for v in all_videos}
        
        recommended = []
        for vid in top_video_ids:
            if vid in video_map:
                v = video_map[vid]
                recommended.append({
                    'id': v['id'],
                    'title': v['title'],
                    'coverUrl': v['coverUrl'],
                    'username': v['username'],
                    'viewCount': v['viewCount'],
                    'tags': v['tags']
                })
        
        return jsonify({
            'success': True,
            'data': recommended,
            'userTags': tag_names[:5],
            'message': f'基于您的收藏兴趣推荐了{len(recommended)}个视频'
        })
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


if __name__ == '__main__':
    print("正在初始化Spark和加载优化版模型...")
    init_spark()
    print("正在加载代理列表...")
    load_proxies()
    print("服务启动中...")
    app.run(host='0.0.0.0', port=5001, debug=False, threaded=True)
