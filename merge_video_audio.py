import requests
import json
import subprocess
import os
import random
import time
import urllib3
from urllib.parse import urlparse

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

PROXY_FILE = 'IP代理.txt'
proxy_list = []
current_proxy_index = 0
max_retries = 3

def load_proxies():
    global proxy_list
    try:
        with open(PROXY_FILE, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            proxy_list = []
            for line in lines:
                line = line.strip()
                if not line:
                    continue
                
                if line.startswith('socks5://'):
                    proxy_list.append({
                        'http': line,
                        'https': line
                    })
                elif line.startswith('socks4://'):
                    proxy_list.append({
                        'http': line,
                        'https': line
                    })
                elif line.startswith('http://') or line.startswith('https://'):
                    proxy_list.append({
                        'http': line,
                        'https': line
                    })
                elif ':' in line:
                    parts = line.split(':')
                    if len(parts) >= 2:
                        ip = parts[0].strip()
                        port = parts[1].strip()
                        proxy_url = f'http://{ip}:{port}'
                        proxy_list.append({
                            'http': proxy_url,
                            'https': proxy_url
                        })
                    else:
                        continue
                else:
                    continue
                
            print(f"已加载 {len(proxy_list)} 个代理")
    except FileNotFoundError:
        print(f"警告: 未找到代理文件 {PROXY_FILE}，将不使用代理")
        proxy_list = []

def get_random_proxy():
    if not proxy_list:
        return None
    return random.choice(proxy_list)

def get_next_proxy():
    global current_proxy_index
    if not proxy_list:
        return None
    proxy = proxy_list[current_proxy_index]
    current_proxy_index = (current_proxy_index + 1) % len(proxy_list)
    return proxy

def test_proxy(proxy, test_url='https://www.bilibili.com', timeout=10):
    if not proxy:
        return True
    try:
        response = requests.get(test_url, proxies=proxy, timeout=timeout, headers={
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })
        return response.status_code == 200
    except:
        return False

def download_file_with_retry(url, filename, headers, max_retries=3):
    global proxy_list
    
    for attempt in range(max_retries):
        proxy = get_random_proxy() if proxy_list else None
        proxy_str = f"使用代理: {proxy['http']}" if proxy else "不使用代理"
        print(f"第 {attempt + 1} 次尝试下载 ({proxy_str})")
        
        try:
            session = requests.Session()
            session.verify = False
            
            response = session.get(
                url, 
                headers=headers, 
                stream=True, 
                proxies=proxy, 
                timeout=15,
                verify=False
            )
            
            if response.status_code == 200:
                total_size = int(response.headers.get('content-length', 0))
                downloaded = 0
                
                with open(filename, 'wb') as f:
                    for chunk in response.iter_content(chunk_size=8192):
                        if chunk:
                            f.write(chunk)
                            downloaded += len(chunk)
                            if total_size > 0:
                                percent = (downloaded / total_size) * 100
                                print(f"\r下载进度: {percent:.1f}%", end='')
                
                print(f"\n下载完成: {filename}")
                return True
            else:
                print(f"下载失败，状态码: {response.status_code}")
                
        except requests.exceptions.SSLError as e:
            print(f"SSL错误，尝试切换代理...")
            continue
        except requests.exceptions.ProxyError as e:
            print(f"代理连接失败，尝试切换代理...")
            continue
        except requests.exceptions.Timeout:
            print(f"请求超时，尝试重新连接...")
            continue
        except requests.exceptions.RequestException as e:
            print(f"下载出错: {e}")
            continue
    
    print(f"下载失败，已达到最大重试次数")
    return False

def merge_video_audio(video_path, audio_path, output_path):
    print(f"正在合并视频和音频...")
    
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
        subprocess.run(cmd, check=True, capture_output=True)
        print(f"合并完成: {output_path}")
        return True
    except subprocess.CalledProcessError as e:
        print(f"合并失败: {e}")
        return False
    except FileNotFoundError:
        print("错误: 未找到ffmpeg，请确保已安装ffmpeg并添加到系统PATH")
        return False

def main():
    json_path = 'bilibili_video_data.json'
    
    headers = {
        "Referer": "https://www.bilibili.com/",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36",
        "Cookie": "buvid3=47677070-488E-5B27-0E1A-C882E333757425995infoc; b_nut=1745932225; _uuid=210F271101-DE4A-103C2-1027C-916411F510EDF26747infoc; enable_web_push=DISABLE; enable_feed_channel=ENABLE; rpdid=|(YYJ)ukkmu0J'u~RlmRukll; buvid_fp=20b4e9c1fcd93b1f9714496753d9d2bf; header_theme_version=OPEN; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; PVID=1; LIVE_BUVID=AUTO6317619205625200; CURRENT_QUALITY=80; buvid4=64CB7880-B1B2-3802-2213-F668646AB63G38993-026022815-2eVVYWuB7swYubBfyNf7XQ%3D%3D; ogv_device_support_hdr=0; ogv_device_support_dolby=0; theme-switch-show=SHOWED; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzQxODA0NDQsImlhdCI6MTc3MzkyMTE4NCwicGx0IjotMX0.37VjDFf9--UtybZnG4Y4ZQqNKEM8Z9P49JHol9QfeIE; bili_ticket_expires=1774180384; SESSDATA=188b2aa4%2C1789473273%2C67e81%2A32CjBD2BPou1OKVe4PWSetKuDvfl7E6ChG7DTI-rpYHhwzHe8y_7gU78Oxor-yim94cyQSVkVsRWRvYlp0eXFmSGN1YUlGbjFEbkxoQllKaFkwcWgtR0tQRk02ZXZ4WTZ5cFNoUkotNG9uR1kySENLclBtemNLMlRTN0F0Qk1zWWFCallGVzVYZVNRIIEC; bili_jct=a7912d1a085208d1b8aa8dd2e0131074; DedeUserID=3546624886311378; DedeUserID__ckMd5=98c8a3d86ab111b0; sid=4y11c1m6; bmg_af_switch=1; bmg_src_def_domain=i0.hdslb.com; _tea_utm_cache_586864={%22creative_id%22:791332005}; CURRENT_FNVAL=2000; home_feed_column=5; browser_resolution=1920-911; bp_t_offset_3546624886311378=1181734743683629056; b_lsid=5D8BC7D6_19D0998360B"
    }
    
    print("=" * 50)
    print("B站视频下载工具 (支持IP代理)")
    print("=" * 50)
    
    load_proxies()
    
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    video_url = data.get('video_url', '')
    audio_url = data.get('audio_url', '')
    bvid = data.get('bvid', 'video')
    title = data.get('title', 'video')
    
    safe_title = "".join(c for c in title if c not in r'\/:*?"<>|')
    
    if not video_url or not audio_url:
        print("错误: JSON文件中缺少video_url或audio_url")
        return
    
    print(f"视频标题: {title}")
    print(f"BV号: {bvid}")
    
    video_file = f'{bvid}_video.m4s'
    audio_file = f'{bvid}_audio.m4s'
    output_file = f'{safe_title}.mp4'
    
    print("\n[1/3] 下载视频流...")
    if not download_file_with_retry(video_url, video_file, headers):
        print("视频流下载失败")
        return
    
    print("\n[2/3] 下载音频流...")
    if not download_file_with_retry(audio_url, audio_file, headers):
        print("音频流下载失败")
        return
    
    print("\n[3/3] 合并音视频...")
    if merge_video_audio(video_file, audio_file, output_file):
        os.remove(video_file)
        os.remove(audio_file)
        print(f"\n临时文件已清理")
        print(f"最终视频: {output_file}")
        print("\n" + "=" * 50)
        print("下载完成!")
        print("=" * 50)

if __name__ == "__main__":
    main()
