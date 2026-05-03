import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { orderService } from "../services/orderService";
import api from "../services/api";

const STATUS_LABELS = {
  PENDING: { label: "Bekliyor", color: "#ff9800" },
  CONFIRMED: { label: "Onaylandi", color: "#2196f3" },
  PAYMENT_PROCESSING: { label: "Odeme Isleniyor", color: "#ff9800" },
  PAYMENT_COMPLETED: { label: "Odendi", color: "#4caf50" },
  PAYMENT_FAILED: { label: "Odeme Basarisiz", color: "#f44336" },
  SHIPPED: { label: "Kargoda", color: "#9c27b0" },
  DELIVERED: { label: "Teslim Edildi", color: "#4caf50" },
  CANCELLED: { label: "Iptal", color: "#f44336" },
};

const STATUS_FLOW = ["PENDING", "CONFIRMED", "PAYMENT_COMPLETED", "SHIPPED", "DELIVERED"];

export default function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    orderService.getById(id)
      .then(res => setOrder(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  const updateStatus = async (status) => {
    setUpdating(true);
    try {
      const res = await api.patch('/api/orders/' + id + '/status?status=' + status);
      setOrder(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setUpdating(false);
    }
  };

  if (loading) return <div style={styles.loading}>Yukleniyor...</div>;
  if (!order) return <div style={styles.loading}>Siparis bulunamadi</div>;

  const currentIndex = STATUS_FLOW.indexOf(order.status);

  return (
    <div style={styles.container}>
      <button onClick={() => navigate("/orders")} style={styles.back}>Siparislerime Don</button>

      <div style={styles.card}>
        <div style={styles.header}>
          <div>
            <h2 style={styles.title}>Siparis #{order.id}</h2>
            <span style={styles.date}>{new Date(order.createdAt).toLocaleDateString("tr-TR")}</span>
          </div>
          <span style={{ ...styles.status, background: STATUS_LABELS[order.status]?.color + "20", color: STATUS_LABELS[order.status]?.color }}>
            {STATUS_LABELS[order.status]?.label || order.status}
          </span>
        </div>

        <div style={styles.timeline}>
          {STATUS_FLOW.map((s, i) => (
            <div key={s} style={styles.step}>
              <div style={{ ...styles.dot, background: i <= currentIndex ? STATUS_LABELS[s]?.color : "#ddd" }} />
              <span style={{ fontSize: "0.75rem", color: i <= currentIndex ? "#333" : "#aaa", marginTop: "4px" }}>
                {STATUS_LABELS[s]?.label}
              </span>
              {i < STATUS_FLOW.length - 1 && (
                <div style={{ ...styles.line, background: i < currentIndex ? "#4caf50" : "#ddd" }} />
              )}
            </div>
          ))}
        </div>

        <div style={styles.section}>
          <h3 style={styles.sectionTitle}>Urunler</h3>
          {order.items.map(item => (
            <div key={item.productId} style={styles.item}>
              <span>{item.productName} x{item.quantity}</span>
              <span>{item.totalPrice?.toLocaleString("tr-TR")} TL</span>
            </div>
          ))}
          <div style={styles.total}>Toplam: <strong>{order.totalAmount?.toLocaleString("tr-TR")} TL</strong></div>
        </div>

        <div style={styles.section}>
          <h3 style={styles.sectionTitle}>Teslimat Adresi</h3>
          <p style={{ color: "#555", margin: 0 }}>{order.shippingAddress}</p>
        </div>

        <div style={styles.section}>
          <h3 style={styles.sectionTitle}>Durumu Guncelle</h3>
          <div style={styles.buttons}>
            {Object.keys(STATUS_LABELS).map(s => (
              <button
                key={s}
                onClick={() => updateStatus(s)}
                disabled={updating || order.status === s}
                style={{
                  ...styles.btn,
                  background: order.status === s ? STATUS_LABELS[s].color : "white",
                  color: order.status === s ? "white" : STATUS_LABELS[s].color,
                  border: "1px solid " + STATUS_LABELS[s].color,
                  opacity: updating ? 0.6 : 1,
                }}
              >
                {STATUS_LABELS[s].label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { padding: "1.5rem", paddingTop: "2rem", maxWidth: "800px", margin: "0 auto" },
  loading: { textAlign: "center", padding: "3rem", color: "#999" },
  back: { background: "none", border: "none", color: "#e53935", cursor: "pointer", fontSize: "0.95rem", marginBottom: "1rem", padding: 0 },
  card: { background: "white", borderRadius: "12px", padding: "1.5rem", boxShadow: "0 2px 8px rgba(0,0,0,0.06)" },
  header: { display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "1.5rem" },
  title: { margin: "0 0 0.3rem 0", color: "#333" },
  date: { color: "#999", fontSize: "0.85rem" },
  status: { padding: "0.3rem 0.8rem", borderRadius: "20px", fontSize: "0.85rem", fontWeight: "600" },
  timeline: { display: "flex", alignItems: "flex-start", marginBottom: "1.5rem", position: "relative" },
  step: { display: "flex", flexDirection: "column", alignItems: "center", flex: 1, position: "relative" },
  dot: { width: "16px", height: "16px", borderRadius: "50%" },
  line: { position: "absolute", top: "8px", left: "50%", width: "100%", height: "2px" },
  section: { borderTop: "1px solid #f5f5f5", paddingTop: "1rem", marginTop: "1rem" },
  sectionTitle: { fontSize: "0.95rem", color: "#333", marginBottom: "0.75rem" },
  item: { display: "flex", justifyContent: "space-between", padding: "0.3rem 0", color: "#555", fontSize: "0.9rem" },
  total: { textAlign: "right", marginTop: "0.5rem", color: "#333" },
  buttons: { display: "flex", flexWrap: "wrap", gap: "0.5rem" },
  btn: { padding: "0.4rem 0.8rem", borderRadius: "20px", fontSize: "0.8rem", cursor: "pointer", fontWeight: "600" },
};