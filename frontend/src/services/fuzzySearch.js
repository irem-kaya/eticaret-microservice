/**
 * Fuzzy Search Service - %75 Eşleşme Algoritması
 * Levenshtein distance + string similarity kullanır
 */

class FuzzySearchService {
  constructor(threshold = 0.75, minMatchLength = 2) {
    this.threshold = threshold;
    this.minMatchLength = minMatchLength;
  }

  /**
   * Levenshtein Distance - İki string arasındaki farkı hesapla
   */
  levenshteinDistance(str1, str2) {
    const s1 = str1.toLowerCase();
    const s2 = str2.toLowerCase();
    const len1 = s1.length;
    const len2 = s2.length;

    const d = Array(len1 + 1)
      .fill(null)
      .map(() => Array(len2 + 1).fill(0));

    for (let i = 0; i <= len1; i++) d[i][0] = i;
    for (let j = 0; j <= len2; j++) d[0][j] = j;

    for (let i = 1; i <= len1; i++) {
      for (let j = 1; j <= len2; j++) {
        const cost = s1[i - 1] === s2[j - 1] ? 0 : 1;
        d[i][j] = Math.min(
          d[i - 1][j] + 1,      // silme
          d[i][j - 1] + 1,      // ekleme
          d[i - 1][j - 1] + cost // değiştirme
        );
      }
    }

    return d[len1][len2];
  }

  /**
   * Similarity Score - 0 ile 1 arasında sonuç (1 = %100 eşleşme)
   */
  similarityScore(str1, str2) {
    const maxLen = Math.max(str1.length, str2.length);
    if (maxLen === 0) return 1;
    const distance = this.levenshteinDistance(str1, str2);
    return 1 - distance / maxLen;
  }

  /**
   * Türkçe karakterleri İngilizce'ye çevir
   */
  normalizeText(text) {
    const turkishChars = {
      ç: "c",
      ğ: "g",
      ı: "i",
      ö: "o",
      ş: "s",
      ü: "u",
      Ç: "C",
      Ğ: "G",
      İ: "I",
      Ö: "O",
      Ş: "S",
      Ü: "U",
    };

    return text.replace(/[çğıöşüÇĞİÖŞÜ]/g, (char) => turkishChars[char] || char);
  }

  /**
   * Arama - Kontrol et ve puanla
   */
  search(query, items, extractText = (item) => item) {
    if (!query || query.length < this.minMatchLength) {
      return items.map((item, index) => ({
        item,
        score: 0,
        index: index,
      }));
    }

    const normalizedQuery = this.normalizeText(query.toLowerCase());

    return items
      .map((item, index) => {
        const text = this.normalizeText(extractText(item).toLowerCase());
        const score = this.similarityScore(normalizedQuery, text);

        return { item, score, index };
      })
      .filter((result) => result.score >= this.threshold)
      .sort((a, b) => b.score - a.score);
  }

  /**
   * Ürün araması - API'dan dönen sonuçları filtrele
   */
  filterProducts(query, products) {
    return this
      .search(query, products, (product) => `${product.name} ${product.description || ""}`)
      .slice(0, 10); // Top 10 results
  }

  /**
   * Kategori araması
   */
  filterCategories(query, categories) {
    return this
      .search(query, categories, (cat) => cat.name)
      .slice(0, 5); // Top 5 results
  }
}

// Singleton instance
const fuzzySearch = new FuzzySearchService();

export default fuzzySearch;

