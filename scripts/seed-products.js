/**
 * Ürün seed scripti
 * Kullanım: node scripts/seed-products.js
 *
 * Gereksinimler:
 *   - Node.js 18+ (native fetch)
 *   - OPENAI_API_KEY env variable (veya aşağıya yapıştırın)
 *   - product-service çalışıyor olmalı (localhost:8082)
 *
 * Örnek:
 *   OPENAI_API_KEY=sk-xxx node scripts/seed-products.js
 *   OPENAI_API_KEY=sk-xxx node scripts/seed-products.js --per-category=20
 */

const OPENAI_API_KEY = process.env.OPENAI_API_KEY || 'sk-replace-with-your-key';
const PRODUCT_SERVICE = 'http://localhost:8082';
const PER_CATEGORY = parseInt(process.argv.find(a => a.startsWith('--per-category='))?.split('=')[1] || '10');

const CATEGORIES = [
  { name: 'Elektronik',               description: 'Telefon, bilgisayar, TV ve elektronik ürünler' },
  { name: 'Giyim',                    description: 'Kadın, erkek ve çocuk giyim ürünleri' },
  { name: 'Ev & Yaşam',               description: 'Mobilya, dekorasyon ve ev tekstili' },
  { name: 'Spor',                     description: 'Spor ekipmanları, kıyafetleri ve aksesuarlar' },
  { name: 'Kitap & Kırtasiye',        description: 'Kitaplar, dergiler ve kırtasiye malzemeleri' },
  { name: 'Oyuncak & Hobi',           description: 'Oyuncaklar, hobi malzemeleri ve oyunlar' },
  { name: 'Kozmetik & Kişisel Bakım', description: 'Makyaj, cilt bakımı ve parfüm' },
  { name: 'Mutfak',                   description: 'Mutfak aletleri, yemek takımları ve pişirme ekipmanları' },
  { name: 'Bahçe & Yapı Market',      description: 'Bahçe aletleri, yapı malzemeleri ve hırdavat' },
  { name: 'Otomobil & Aksesuar',      description: 'Araç aksesuarları, bakım ürünleri ve yedek parça' },
];

async function generateProducts(categoryName, count) {
  const prompt = `Generate ${count} realistic Turkish e-commerce products for the "${categoryName}" category.
Return ONLY a valid JSON array with no other text, no markdown, no code blocks.
Each item must follow this exact structure:
[{"name":"product name","description":"2-3 sentence Turkish description","price":123.45,"stock":50}]
Requirements:
- Names must be realistic product names (brand + model style) in Turkish
- Descriptions must be in Turkish, 2-3 sentences
- Prices must be realistic for Turkish market (TRY)
- Stock must be between 10 and 200
- All products must be different`;

  const response = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${OPENAI_API_KEY}`,
    },
    body: JSON.stringify({
      model: 'gpt-4o-mini',
      messages: [{ role: 'user', content: prompt }],
      max_tokens: 3000,
    }),
  });

  if (!response.ok) {
    const err = await response.text();
    throw new Error(`OpenAI error: ${err}`);
  }

  const data = await response.json();
  const content = data.choices[0].message.content.trim();

  // Clean markdown code blocks if present
  let json = content;
  if (json.startsWith('```json')) json = json.slice(7);
  else if (json.startsWith('```')) json = json.slice(3);
  if (json.endsWith('```')) json = json.slice(0, -3);

  return JSON.parse(json.trim());
}

async function getOrCreateCategory(cat) {
  // Fetch existing categories
  const listRes = await fetch(`${PRODUCT_SERVICE}/api/categories`);
  const listData = await listRes.json();
  const existing = (listData.data || []).find(c => c.name === cat.name);
  if (existing) {
    console.log(`  Kategori mevcut: ${cat.name} (ID: ${existing.id})`);
    return existing.id;
  }

  // Create new category
  const createRes = await fetch(`${PRODUCT_SERVICE}/api/categories`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: cat.name, description: cat.description }),
  });
  const createData = await createRes.json();
  const id = createData.data?.id;
  console.log(`  Kategori oluşturuldu: ${cat.name} (ID: ${id})`);
  return id;
}

async function createProduct(product, categoryId) {
  const res = await fetch(`${PRODUCT_SERVICE}/api/products`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name: product.name,
      description: product.description || '',
      price: product.price,
      stock: product.stock || 50,
      categoryId,
    }),
  });
  return res.ok;
}

async function main() {
  console.log(`\n🚀 Seed başlıyor: ${CATEGORIES.length} kategori × ${PER_CATEGORY} ürün = ${CATEGORIES.length * PER_CATEGORY} toplam ürün\n`);

  if (OPENAI_API_KEY.startsWith('sk-replace')) {
    console.error('❌ OPENAI_API_KEY ayarlanmamış! OPENAI_API_KEY=sk-xxx node scripts/seed-products.js');
    process.exit(1);
  }

  let totalCreated = 0;
  let totalFailed = 0;

  for (const cat of CATEGORIES) {
    console.log(`\n📦 Kategori: ${cat.name}`);

    let categoryId;
    try {
      categoryId = await getOrCreateCategory(cat);
    } catch (e) {
      console.error(`  ❌ Kategori hatası: ${e.message}`);
      continue;
    }

    let products = [];
    const BATCH = 20;
    let remaining = PER_CATEGORY;

    while (remaining > 0) {
      const batchSize = Math.min(remaining, BATCH);
      try {
        const batch = await generateProducts(cat.name, batchSize);
        products = products.concat(batch);
        process.stdout.write(`  AI: ${products.length}/${PER_CATEGORY} ürün üretildi\r`);
      } catch (e) {
        console.error(`\n  ❌ AI üretim hatası: ${e.message}`);
      }
      remaining -= batchSize;
    }

    console.log(`\n  📝 ${products.length} ürün veritabanına ekleniyor...`);
    let catCreated = 0;
    let catFailed = 0;

    for (const product of products) {
      const ok = await createProduct(product, categoryId);
      if (ok) catCreated++;
      else catFailed++;
    }

    totalCreated += catCreated;
    totalFailed += catFailed;
    console.log(`  ✅ ${catCreated} eklendi, ❌ ${catFailed} başarısız`);
  }

  console.log(`\n🎉 Seed tamamlandı!`);
  console.log(`   Başarılı: ${totalCreated} ürün`);
  console.log(`   Başarısız: ${totalFailed} ürün`);
}

main().catch(console.error);