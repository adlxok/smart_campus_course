import requests
import json
import subprocess
import os

def download_file(url, filename, headers):
    print(f"正在下载: {filename}")
    response = requests.get(url, headers=headers, stream=True)
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
        "Cookie": "buvid3=47677070-488E-5B27-0E1A-C882E333757425995infoc; b_nut=1745932225; _uuid=210F271101-DE4A-103C2-1027C-916411F510EDF26747infoc; enable_web_push=DISABLE; enable_feed_channel=ENABLE; rpdid=|(YYJ)ukkmu0J'u~RlmRukll; DedeUserID=3546624886311378; DedeUserID__ckMd5=98c8a3d86ab111b0; buvid_fp=20b4e9c1fcd93b1f9714496753d9d2bf; header_theme_version=OPEN; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; PVID=1; LIVE_BUVID=AUTO6317619205625200; CURRENT_QUALITY=80; buvid4=64CB7880-B1B2-3802-2213-F668646AB63G38993-026022815-2eVVYWuB7swYubBfyNf7XQ%3D%3D; ogv_device_support_hdr=0; ogv_device_support_dolby=0; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzM4MzA2MjEsImlhdCI6MTc3MzU3MTM2MSwicGx0IjotMX0._H1Xzx18u6mDyY_UQycw7Vi5Ctsxnrcsk4DT2pwV07g; bili_ticket_expires=1773830561; bmg_af_switch=1; bmg_src_def_domain=i0.hdslb.com; home_feed_column=5; browser_resolution=1920-911; SESSDATA=ed58402d%2C1789215749%2C92fed%2A32CjDndERecaqXHhtLNa5F6EzUKBXXVM1L25WGHmpKHEaghtKmqGIUrMYJP6y1BQhEFc4SVlhmTDZLcVNHbWN1dWNsUTRhM2RPTGw2THNySFEyVnhSYU9zb0lKemtGZEJsNHdvZDJOM1lXNWpBekdid2pFQzFLeEEwUm1tdC1UU1dDRGg3NXNmcXRBIIEC; bili_jct=4b1f0825219053527830efdd4b202f13; sid=5zqb93e6; bp_t_offset_3546624886311378=1180678765844365312; CURRENT_FNVAL=4048; b_lsid=9611C1B9_19CFB40CB53"
    }
    
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
    
    download_file(video_url, video_file, headers)
    download_file(audio_url, audio_file, headers)
    
    if merge_video_audio(video_file, audio_file, output_file):
        os.remove(video_file)
        os.remove(audio_file)
        print(f"\n临时文件已清理")
        print(f"最终视频: {output_file}")

if __name__ == "__main__":
    main()
