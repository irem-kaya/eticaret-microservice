import requests

# AI Service adresi
URL = "http://localhost:8080/api/ai/process-scraped" # Gateway üzerinden gidiyoruz

raw_products = [
    {
        "rawName": "Apple iPhone 15 Pro Max 256GB",
        "categoryName": "Elektronik",
        "categoryId": 1,
        "price": 84999.00,
        "sourceUrl": "https://www.n11.com/urun/iphone-15-pro-max"
    },
    {
        "rawName": "Stanley Quencher Termos 1.18L",
        "categoryName": "Ev & Yasam",
        "categoryId": 3,
        "price": 2499.00,
        "sourceUrl": "https://www.trendyol.com/stanley/quencher-termos"
    }
]

try:
    response = requests.post(URL, json=raw_products)
    print(f"Durum Kodu: {response.status_code}")
    print(f"Sunucu Yanıtı: {response.text}")
except Exception as e:
    print(f"Hata: {e}")