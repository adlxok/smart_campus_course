import asyncio
import json
import re
import pymysql
import requests
from playwright.async_api import async_playwright

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

MAX_VIDEOS = 1000

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

async def crawl_bilibili_music():
    print("=" * 60)
    print(f"Playwright 自动化爬取 B站音乐区 (目标: {MAX_VIDEOS}条)")
    print("=" * 60)
    
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context()
        
        await context.add_cookies([
            {"name": "SESSDATA", "value": "ed58402d%2C1789215749%2C92fed%2A32CjDndERecaqXHhtLNa5F6EzUKBXXVM1L25WGHmpKHEaghtKmqGIUrMYJP6y1BQhEFc4SVlhmTDZLcVNHbWN1dWNsUTRhM2RPTGw2THNySFEyVnhSYU9zb0lKemtGZEJsNHdvZDJOM1lXNWpBekdid2pFQzFLeEEwUm1tdC1UU1dDRGg3NXNmcXRBIIEC", "domain": ".bilibili.com", "path": "/"}
        ])
        
        page = await context.new_page()
        
        print("\n[1] 打开B站音乐区页面...")
        await page.goto("https://www.bilibili.com/c/music/", wait_until="networkidle")
        await page.wait_for_timeout(2000)
        
        print("[2] 滚动页面获取视频链接...")
        video_links = set()
        scroll_count = 0
        max_scrolls = 100
        no_new_links_count = 0
        
        while len(video_links) < MAX_VIDEOS and scroll_count < max_scrolls:
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
                print(f"  连续5次滚动无新链接，停止滚动")
                break
            
            scroll_count += 1
            print(f"  滚动 {scroll_count} 次，已获取 {len(video_links)} 个链接")
            
            await page.evaluate('window.scrollTo(0, document.body.scrollHeight)')
            await page.wait_for_timeout(1500)
        
        unique_links = list(video_links)[:MAX_VIDEOS]
        print(f"\n[3] 共获取 {len(unique_links)} 个视频链接，准备爬取\n")
        
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
                print(f"\n  [进度] 已爬取 {success_count} 条，失败 {fail_count} 条\n")
            
            print()
        
        await browser.close()
        
        print("\n" + "=" * 60)
        print(f"爬取完成!")
        print(f"  成功: {success_count} 条")
        print(f"  失败: {fail_count} 条")
        print("=" * 60)

if __name__ == "__main__":
    asyncio.run(crawl_bilibili_music())
