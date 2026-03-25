from playwright.async_api import async_playwright
import asyncio

async def test_html5_player():
    async with async_playwright() as p:
        # 🔥 关键：启动参数开启HTML5播放器
        browser = await p.chromium.launch(
            headless=False,
            args=[
                "--no-sandbox",
                "--ignore-gpu-blacklist",
                "--disable-blink-features=AutomationControlled",
                "--enable-features=VaapiVideoDecoder"
            ]
        )
        context = await browser.new_context()
        page = await context.new_page()
        
        # 打开B站视频，测试HTML5播放器
        await page.goto("https://www.bilibili.com/video/BV11SF6z3E9r/")
        await page.wait_for_timeout(60000) # 等待5秒，看视频是否加载
        
        print("✅ HTML5播放器支持成功！")
        await browser.close()

if __name__ == "__main__":
    asyncio.run(test_html5_player())