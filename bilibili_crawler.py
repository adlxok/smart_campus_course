import requests
import json
import re
import pymysql

url = "https://www.bilibili.com/video/BV1cbcrzBEAg"

headers = {
    "Referer": "https://www.bilibili.com/",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36",
    "Cookie": "buvid3=47677070-488E-5B27-0E1A-C882E333757425995infoc; b_nut=1745932225; _uuid=210F271101-DE4A-103C2-1027C-916411F510EDF26747infoc; enable_web_push=DISABLE; enable_feed_channel=ENABLE; rpdid=|(YYJ)ukkmu0J'u~RlmRukll; DedeUserID=3546624886311378; DedeUserID__ckMd5=98c8a3d86ab111b0; buvid_fp=20b4e9c1fcd93b1f9714496753d9d2bf; header_theme_version=OPEN; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; PVID=1; LIVE_BUVID=AUTO6317619205625200; CURRENT_QUALITY=80; buvid4=64CB7880-B1B2-3802-2213-F668646AB63G38993-026022815-2eVVYWuB7swYubBfyNf7XQ%3D%3D; ogv_device_support_hdr=0; ogv_device_support_dolby=0; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzM4MzA2MjEsImlhdCI6MTc3MzU3MTM2MSwicGx0IjotMX0._H1Xzx18u6mDyY_UQycw7Vi5Ctsxnrcsk4DT2pwV07g; bili_ticket_expires=1773830561; bmg_af_switch=1; bmg_src_def_domain=i0.hdslb.com; home_feed_column=5; browser_resolution=1920-911; SESSDATA=ed58402d%2C1789215749%2C92fed%2A32CjDndERecaqXHhtLNa5F6EzUKBXXVM1L25WGHmpKHEaghtKmqGIUrMYJP6y1BQhEFc4SVlhmTDZLcVNHbWN1dWNsUTRhM2RPTGw2THNySFEyVnhSYU9zb0lKemtGZEJsNHdvZDJOM1lXNWpBekdid2pFQzFLeEEwUm1tdC1UU1dDRGg3NXNmcXRBIIEC; bili_jct=4b1f0825219053527830efdd4b202f13; sid=5zqb93e6; bp_t_offset_3546624886311378=1180678765844365312; CURRENT_FNVAL=4048; b_lsid=9611C1B9_19CFB40CB53"
}

db_config = {
    "host": "localhost",
    "user": "root",
    "password": "root",
    "database": "smart_campus_course",
    "charset": "utf8mb4"
}

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
        title = VALUES(title),
        tags = VALUES(tags),
        category = VALUES(category),
        cover = VALUES(cover),
        video_url = VALUES(video_url),
        audio_url = VALUES(audio_url),
        view_count = VALUES(view_count),
        danmaku_count = VALUES(danmaku_count),
        like_count = VALUES(like_count),
        coin_count = VALUES(coin_count),
        favorite_count = VALUES(favorite_count),
        share_count = VALUES(share_count),
        reply_count = VALUES(reply_count),
        update_time = CURRENT_TIMESTAMP
        """
        
        cursor.execute(sql, (
            video_info['bvid'],
            video_info['title'],
            tags_json,
            video_info['category'],
            video_info['cover'],
            video_info['video_url'],
            video_info['audio_url'],
            video_info['view'],
            video_info['danmaku'],
            video_info['like'],
            video_info['coin'],
            video_info['favorite'],
            video_info['share'],
            video_info['reply']
        ))
        
        conn.commit()
        print(f"\n数据已保存到MySQL数据库 bilibili_video 表")
        
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"\n数据库保存失败: {e}")

def crawl_bilibili():
    print("=" * 60)
    print("开始爬取B站视频页面")
    print("=" * 60)
    
    session = requests.Session()
    response = session.get(url=url, headers=headers, timeout=30)
    html = response.text
    
    keyword_match = re.search(r'<meta.*?name="keywords".*?content="(.*?)"', html)
    keyword_str = keyword_match.group(1) if keyword_match else ""
    words = keyword_str.split(",")
    clean_words = words[:-4]
    category = clean_words[-1] if clean_words else "未找到"
    
    title = re.findall(r'<title>(.*?)_哔哩哔哩_bilibili</title>', html)
    video_title = title[0] if title else "未知"
    
    playinfo_match = re.findall(r'window\.__playinfo__\s*=\s*(\{.*?\})\s*</script>', html, re.DOTALL)
    if playinfo_match:
        playinfo = json.loads(playinfo_match[0])
        video_url = playinfo['data']['dash']['video'][0]['baseUrl']
        audio_url = playinfo['data']['dash']['audio'][0]['baseUrl']
    else:
        video_url = ""
        audio_url = ""
    
    initial_state_match = re.findall(r'window\.__INITIAL_STATE__\s*=\s*(\{.*?\});', html, re.DOTALL)
    if initial_state_match:
        initial_state = json.loads(initial_state_match[0])
        video_data = initial_state.get('videoData', {})
        stat = video_data.get('stat', {})
        
        bvid = video_data.get('bvid', '')
        cover = video_data.get('pic', '')
        
        tags_list = initial_state.get('tags', [])
        tags = [tag.get('tag_name', '') for tag in tags_list if tag.get('tag_name')]
        
        view = stat.get('view', 0)
        danmaku = stat.get('danmaku', 0)
        like = stat.get('like', 0)
        coin = stat.get('coin', 0)
        favorite = stat.get('favorite', 0)
        share = stat.get('share', 0)
        reply = stat.get('reply', 0)
    else:
        bvid = ""
        cover = ""
        tags = []
        view = danmaku = like = coin = favorite = share = reply = 0
    
    video_info = {
        "bvid": bvid,
        "title": video_title,
        "tags": tags,
        "category": category,
        "cover": cover,
        "video_url": video_url,
        "audio_url": audio_url,
        "view": view,
        "danmaku": danmaku,
        "like": like,
        "coin": coin,
        "favorite": favorite,
        "share": share,
        "reply": reply
    }
    
    print("\n" + "=" * 60)
    print("爬取结果")
    print("=" * 60)
    print(f"\n【BV号】: {bvid}")
    print(f"\n【视频标题】: {video_title}")
    print(f"\n【视频标签】: {', '.join(tags)}")
    print(f"\n【视频分类】: {category}")
    print(f"\n【视频封面】: {cover}")
    print(f"\n【视频URL】: {video_url[:100]}..." if video_url else "\n【视频URL】: 未获取")
    print(f"\n【音频URL】: {audio_url[:100]}..." if audio_url else "\n【音频URL】: 未获取")
    print(f"\n【播放量】: {view:,}")
    print(f"\n【弹幕量】: {danmaku:,}")
    print(f"\n【点赞数】: {like:,}")
    print(f"\n【投币数】: {coin:,}")
    print(f"\n【收藏数】: {favorite:,}")
    print(f"\n【分享数】: {share:,}")
    print(f"\n【评论数】: {reply:,}")
    
    save_to_mysql(video_info)
    
    print("\n" + "=" * 60)
    print("爬取完成!")
    print("=" * 60)

if __name__ == "__main__":
    crawl_bilibili()
