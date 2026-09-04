import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { cancelOrder, fetchOrder } from "../api/orders";
import { Order } from "../types/order";

export function OrderDetailPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [working, setWorking] = useState(false);

  useEffect(() => {
    if (!orderId) {
      setErrorMessage("Missing order id.");
      setLoading(false);
      return;
    }

    void loadOrder(orderId);
  }, [orderId]);

  async function loadOrder(id: string) {
    try {
      setLoading(true);
      setErrorMessage("");
      const data = await fetchOrder(id);
      setOrder(data);
    } catch (error) {
      setErrorMessage("Unable to load the requested order.");
    } finally {
      setLoading(false);
    }
  }

  async function handleCancel() {
    if (!order) {
      return;
    }

    try {
      setWorking(true);
      const updated = await cancelOrder(order.id);
      setOrder(updated);
    } catch (error) {
      setErrorMessage("Cancellation failed. The order may no longer be pending.");
    } finally {
      setWorking(false);
    }
  }

  return (
    <section className="panel detail-panel">
      <div className="panel-heading">
        <div>
          <p className="section-label">Detail</p>
          <h2>Order Overview</h2>
        </div>
        <div className="action-row">
          <button type="button" className="ghost-button" onClick={() => navigate(-1)}>
            Back
          </button>
          <Link to="/" className="ghost-link">
            Dashboard
          </Link>
        </div>
      </div>

      {loading ? <p className="placeholder">Loading order...</p> : null}
      {errorMessage ? <p className="message error">{errorMessage}</p> : null}

      {!loading && order ? (
        <div className="stack">
          <div className="detail-grid">
            <div className="detail-card">
              <span className="detail-label">Order ID</span>
              <strong>{order.id}</strong>
            </div>
            <div className="detail-card">
              <span className="detail-label">Customer</span>
              <strong>{order.customerId}</strong>
            </div>
            <div className="detail-card">
              <span className="detail-label">Status</span>
              <strong>{order.status}</strong>
            </div>
            <div className="detail-card">
              <span className="detail-label">Total</span>
              <strong>${order.totalAmount.toFixed(2)}</strong>
            </div>
          </div>

          <div className="meta-row">
            <span>Created: {new Date(order.createdAt).toLocaleString()}</span>
            <span>Updated: {new Date(order.updatedAt).toLocaleString()}</span>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Name</th>
                  <th>Qty</th>
                  <th>Unit Price</th>
                  <th>Line Total</th>
                </tr>
              </thead>
              <tbody>
                {order.items.map((item) => (
                  <tr key={item.id ?? item.productId}>
                    <td>{item.productId}</td>
                    <td>{item.productName}</td>
                    <td>{item.quantity}</td>
                    <td>${item.unitPrice.toFixed(2)}</td>
                    <td>${(item.lineTotal ?? 0).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {order.status === "PENDING" ? (
            <div className="action-row">
              <button type="button" className="danger-button" disabled={working} onClick={handleCancel}>
                {working ? "Cancelling..." : "Cancel Order"}
              </button>
            </div>
          ) : null}
        </div>
      ) : null}
    </section>
  );
}
