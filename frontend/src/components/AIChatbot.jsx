import { useState, useRef, useEffect } from 'react';

const BOT_NAME = 'Pınar';
const BOT_ICON = '🐞';
const GEMINI_KEY = import.meta.env.VITE_GEMINI_KEY;

const SYSTEM = `Sen TMarket Pro e-ticaret sitesinin AI asistanı Pınar'sın.
Türkçe konuş, samimi ve yardımsever ol. Kısa ve net cevaplar ver (max 3 cümle).
Kategoriler: Elektronik, Giyim & Moda, Ev & Yaşam, Spor & Outdoor.
Ürünler: Apple iPhone 15 Pro (74999₺), Samsung Galaxy S24 Ultra (54999₺), MacBook Air M2 (42999₺), Sony WH-1000XM5 (12999₺), Nike Air Force 1 (3299₺), Levis 501 Jean (1899₺), Dyson V15 (19999₺), Garmin Forerunner 265 (14999₺).
Ürün önerirken: 🛍️ [Ürün] - [Fiyat]₺ formatını kullan.`;

export default function AIChatbot() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'assistant', content: `Merhaba! Ben Pınar 🐞 TMarket Pro AI asistanınım. Size ürün önerileri yapabilir, arama yapmanıza yardımcı olabilirim. Nasıl yardımcı olabilirim?` }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const quickReplies = ['🛍️ Ürün öner', '📱 Elektronik', '💰 Uygun fiyat', '⭐ Popüler ürünler'];

  const sendMessage = async (text) => {
    const userMsg = text || input;
    if (!userMsg.trim() || loading) return;

    const newMessages = [...messages, { role: 'user', content: userMsg }];
    setMessages(newMessages);
    setInput('');
    setLoading(true);

    try {
      // Sadece son 6 mesajı gönder — rate limit için
      const recentMessages = newMessages.slice(-6);

      const contents = recentMessages.map(m => ({
        role: m.role === 'assistant' ? 'model' : 'user',
        parts: [{ text: m.content }]
      }));

      const response = await fetch(
        // Bunu dene:
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=${GEMINI_KEY}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            system_instruction: { parts: [{ text: SYSTEM }] },
            contents,
            generationConfig: { maxOutputTokens: 10000, temperature: 0.7 }
          })
        }
      );

      const data = await response.json();

      if (data.error) {
        if (data.error.code === 429) {
          setMessages(prev => [...prev, { role: 'assistant', content: 'Çok fazla istek geldi, lütfen 1 dakika bekleyip tekrar deneyin. ⏳' }]);
        } else {
          setMessages(prev => [...prev, { role: 'assistant', content: 'Bir hata oluştu, tekrar deneyin. 🙏' }]);
        }
        return;
      }

      const reply = data.candidates?.[0]?.content?.parts?.[0]?.text || 'Yanıt alınamadı.';
      setMessages(prev => [...prev, { role: 'assistant', content: reply }]);
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Bağlantı sorunu. Lütfen tekrar deneyin. 🙏' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {open && (
        <div style={s.window}>
          <div style={s.header}>
            <div style={s.headerLeft}>
              <div style={s.botAvatar}>{BOT_ICON}</div>
              <div>
                <div style={s.botName}>{BOT_NAME}</div>
                <div style={s.botStatus}>● Online · TMarket Asistan</div>
              </div>
            </div>
            <button style={s.closeBtn} onClick={() => setOpen(false)}>✕</button>
          </div>

          <div style={s.messages}>
            {messages.map((msg, i) => (
              <div key={i} style={{ ...s.msgRow, justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start' }}>
                {msg.role === 'assistant' && <div style={s.msgAvatar}>{BOT_ICON}</div>}
                <div style={{ ...s.bubble, ...(msg.role === 'user' ? s.userBubble : s.aiBubble) }}>
                  {msg.content}
                </div>
              </div>
            ))}
            {loading && (
              <div style={{ ...s.msgRow, justifyContent: 'flex-start' }}>
                <div style={s.msgAvatar}>{BOT_ICON}</div>
                <div style={{ ...s.aiBubble, ...s.bubble, color: '#999' }}>Yazıyor...</div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {messages.length === 1 && (
            <div style={s.quickReplies}>
              {quickReplies.map(q => (
                <button key={q} style={s.quickReply} onClick={() => sendMessage(q)}>{q}</button>
              ))}
            </div>
          )}

          <div style={s.inputArea}>
            <input
              style={s.chatInput}
              placeholder="Pınar'a sorun..."
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && !loading && sendMessage()}
            />
            <button style={{ ...s.sendBtn, opacity: loading || !input.trim() ? 0.5 : 1 }}
              onClick={() => sendMessage()} disabled={loading || !input.trim()}>
              <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                <path d="m22 2-7 20-4-9-9-4 20-7z"/>
              </svg>
            </button>
          </div>
        </div>
      )}

      <button style={s.bubbleBtn} onClick={() => setOpen(!open)}>
        <span style={{ fontSize: open ? '18px' : '26px' }}>{open ? '✕' : BOT_ICON}</span>
        {!open && messages.length > 1 && <span style={s.notifDot} />}
      </button>
    </>
  );
}

const s = {
  window: { position: 'fixed', bottom: '90px', right: '24px', width: '360px', maxHeight: '540px', background: '#fff', borderRadius: '20px', boxShadow: '0 20px 60px rgba(0,0,0,0.18)', display: 'flex', flexDirection: 'column', overflow: 'hidden', zIndex: 1000 },
  header: { background: 'linear-gradient(135deg, #c62828, #e53935)', padding: '16px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' },
  headerLeft: { display: 'flex', alignItems: 'center', gap: '12px' },
  botAvatar: { width: '40px', height: '40px', borderRadius: '50%', background: 'rgba(255,255,255,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '22px' },
  botName: { color: '#fff', fontWeight: '700', fontSize: '15px' },
  botStatus: { color: 'rgba(255,255,255,0.75)', fontSize: '11px', marginTop: '2px' },
  closeBtn: { background: 'rgba(255,255,255,0.2)', border: 'none', color: '#fff', cursor: 'pointer', width: '28px', height: '28px', borderRadius: '50%', fontSize: '13px', fontWeight: '700', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  messages: { flex: 1, overflowY: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '10px', minHeight: '200px', maxHeight: '300px' },
  msgRow: { display: 'flex', alignItems: 'flex-end', gap: '8px' },
  msgAvatar: { width: '28px', height: '28px', borderRadius: '50%', background: '#fce4ec', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '15px', flexShrink: 0 },
  bubble: { maxWidth: '78%', padding: '10px 14px', borderRadius: '16px', fontSize: '13px', lineHeight: '1.6', whiteSpace: 'pre-wrap' },
  aiBubble: { background: '#f5f5f5', color: '#333', borderBottomLeftRadius: '4px' },
  userBubble: { background: '#c62828', color: '#fff', borderBottomRightRadius: '4px' },
  quickReplies: { padding: '0 16px 10px', display: 'flex', flexWrap: 'wrap', gap: '6px' },
  quickReply: { background: '#fff3e0', border: '1px solid #ffcc80', borderRadius: '20px', padding: '6px 12px', fontSize: '12px', cursor: 'pointer', color: '#bf360c', fontWeight: '500' },
  inputArea: { display: 'flex', gap: '8px', padding: '12px 16px', borderTop: '1px solid #f0f0f0', background: '#fafafa' },
  chatInput: { flex: 1, border: '1.5px solid #e8e8e8', borderRadius: '12px', padding: '10px 14px', fontSize: '13px', outline: 'none', background: '#fff' },
  sendBtn: { background: '#c62828', border: 'none', borderRadius: '12px', padding: '10px 14px', color: '#fff', cursor: 'pointer', display: 'flex', alignItems: 'center' },
  bubbleBtn: { position: 'fixed', bottom: '24px', right: '24px', width: '58px', height: '58px', borderRadius: '50%', background: 'linear-gradient(135deg, #c62828, #e53935)', color: '#fff', border: 'none', cursor: 'pointer', fontSize: '26px', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 20px rgba(198,40,40,0.45)', zIndex: 1000 },
  notifDot: { position: 'absolute', top: '6px', right: '6px', width: '12px', height: '12px', borderRadius: '50%', background: '#ff5722', border: '2px solid #fff' },
};