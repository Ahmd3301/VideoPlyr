const axios = require('axios');
const cheerio = require('cheerio');
const { JSDOM, VirtualConsole } = require('jsdom');

const mainUrl = process.argv[2];

if (!mainUrl) {
    console.log("Usage: node link.js <PAGE_URL>");
    process.exit(1);
}

async function getPlayerUrl(url) {
    try {
        const { data } = await axios.get(url, {
            headers: { 'User-Agent': 'Mozilla/5.0' }
        });
        const $ = cheerio.load(data);
        const firstLi = $('li[onclick*="player_iframe.location.href"]').first();
        const onClickAttr = firstLi.attr('onclick');
        if (onClickAttr) {
            const match = onClickAttr.match(/'([^']+)'/);
            return match ? match[1] : null;
        }
        return null;
    } catch (error) {
        return null;
    }
}

async function extractM3u8(playerUrl) {
    console.log(`[*] Fetching URL: ${playerUrl}`);

    const response = await fetch(playerUrl, {
        headers: {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
            'Referer': 'https://faselhd.center/' 
        }
    });

    const html = await response.text();

    return new Promise((resolve) => {
        const virtualConsole = new VirtualConsole();
        let checked = false;

        const dom = new JSDOM(html, {
            url: playerUrl,
            runScripts: "dangerously",
            resources: "usable",
            virtualConsole
        });

        const interval = setInterval(() => {
            if (checked) return;

            const buttons = dom.window.document.querySelectorAll('button.hd_btn');
            for (let btn of buttons) {
                const dataUrl = btn.getAttribute('data-url');
                if (dataUrl && dataUrl.includes('.m3u8')) {
                    checked = true;
                    clearInterval(interval);
                    clearTimeout(timeout);
                    dom.window.close(); // إغلاق النافذة لتحرير الذاكرة
                    resolve(dataUrl);
                    return;
                }
            }
        }, 300);

        const timeout = setTimeout(() => {
            checked = true;
            clearInterval(interval);
            dom.window.close();
            resolve(null);
        }, 10000);
    });
}

async function start() {
    const playerUrl = await getPlayerUrl(mainUrl);
    
    if (!playerUrl) {
        console.log("[-] Could not find Player URL.");
        process.exit(1);
    }

    const m3u8 = await extractM3u8(playerUrl);

    if (m3u8) {
        console.log("[+] Found m3u8 Link in the DOM!");
        console.log("M3u8---");
        console.log(m3u8);
        console.log("---");
        process.exit(0); // إنهاء العملية بنجاح
    } else {
        console.log("[-] Could not find the m3u8 link.");
        process.exit(1); // إنهاء العملية مع خطأ
    }
}

start();
