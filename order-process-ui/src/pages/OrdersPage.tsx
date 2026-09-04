import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { createOrder, fetchOrders } from "../api/orders";
import { CreateOrderPayload, Order, OrderStatus } from "../types/order";

type OrderFormState = {
  customerId: string;
  items: Array<{
    id: string;
    productId: string;
    productName: string;
    quantity: string;
    unitPrice: string;
  }>;
};

function createOrderItemForm() {
  return {
    id: crypto.randomUUID(),
    productId: "",
    productName: "",
    quantity: "1",
    unitPrice: "0"
  };
}

function createInitialFormState(): OrderFormState {
  return {
    customerId: "",
    items: [createOrderItemForm()]
  };
}

const statusOptions: Array<OrderStatus | ""> = ["", "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"];

export function OrdersPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "">("");
  const [orderIdSearch, setOrderIdSearch] = useState("");
  const [formState, setFormState] = useState<OrderFormState>(createInitialFormState);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    void loadOrders(statusFilter);
  }, [statusFilter]);

  async function loadOrders(status?: OrderStatus | "") {
    try {
      setLoading(true);
      setErrorMessage("");
      const data = await fetchOrders(status);
      setOrders(data);
    } catch (error) {
      setErrorMessage("Unable to load orders. Make sure the order service is running.");
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      const payload: CreateOrderPayload = {
        customerId: formState.customerId,
        items: formState.items.map((item) => ({
          productId: item.productId,
          productName: item.productName,
          quantity: Number(item.quantity),
          unitPrice: Number(item.unitPrice)
        }))
      };

      const createdOrder = await createOrder(payload);
      setSuccessMessage(`Order ${createdOrder.id} created successfully.`);
      setFormState(createInitialFormState());
      await loadOrders(statusFilter);
    } catch (error) {
      setErrorMessage("Order creation failed. Review the form values and try again.");
    } finally {
      setSubmitting(false);
    }
  }

  function updateItem(index: number, field: string, value: string) {
    setFormState((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item
      )
    }));
  }

  function addItemRow() {
    setFormState((current) => ({
      ...current,
      items: [
        ...current.items,
        createOrderItemForm()
      ]
    }));
  }

  function removeItemRow(index: number) {
    setFormState((current) => ({
      ...current,
      items: current.items.filter((_, itemIndex) => itemIndex !== index)
    }));
  }

  function handleOrderSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const orderId = orderIdSearch.trim();

    if (orderId) {
      navigate(`/orders/${orderId}`);
    }
  }

  return (
    <div className="dashboard-grid">
      <section className="panel">
        <div className="panel-heading">
          <div>
            <p className="section-label">Create</p>
            <h2>New Order</h2>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="stack">
          <label className="field">
            <span>Customer ID</span>
            <input
              value={formState.customerId}
              onChange={(event) =>
                setFormState((current) => ({ ...current, customerId: event.target.value }))
              }
              placeholder="CUST-1001"
              required
            />
          </label>

          <div className="stack">
            {formState.items.map((item, index) => (
              <div className="item-card" key={item.id}>
                <div className="item-grid">
                  <label className="field">
                    <span>Product ID</span>
                    <input
                      value={item.productId}
                      onChange={(event) => updateItem(index, "productId", event.target.value)}
                      placeholder="P-101"
                      required
                    />
                  </label>
                  <label className="field">
                    <span>Product Name</span>
                    <input
                      value={item.productName}
                      onChange={(event) => updateItem(index, "productName", event.target.value)}
                      placeholder="Keyboard"
                      required
                    />
                  </label>
                  <label className="field">
                    <span>Quantity</span>
                    <input
                      type="number"
                      min="1"
                      value={item.quantity}
                      onChange={(event) => updateItem(index, "quantity", event.target.value)}
                      required
                    />
                  </label>
                  <label className="field">
                    <span>Unit Price</span>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={item.unitPrice}
                      onChange={(event) => updateItem(index, "unitPrice", event.target.value)}
                      required
                    />
                  </label>
                </div>

                {formState.items.length > 1 ? (
                  <button
                    type="button"
                    className="ghost-button"
                    onClick={() => removeItemRow(index)}
                  >
                    Remove Item
                  </button>
                ) : null}
              </div>
            ))}
          </div>

          <div className="action-row">
            <button type="button" className="ghost-button" onClick={addItemRow}>
              Add Item
            </button>
            <button type="submit" className="primary-button" disabled={submitting}>
              {submitting ? "Creating..." : "Create Order"}
            </button>
          </div>
        </form>
      </section>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <p className="section-label">Monitor</p>
            <h2>Orders</h2>
          </div>
          <label className="field compact-field">
            <span>Status Filter</span>
            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value as OrderStatus | "")}
            >
              {statusOptions.map((option) => (
                <option key={option || "ALL"} value={option}>
                  {option || "ALL"}
                </option>
              ))}
            </select>
          </label>
        </div>

        <form className="order-search" onSubmit={handleOrderSearch}>
          <label className="field">
            <span>Find Order by ID</span>
            <input
              type="number"
              min="1"
              value={orderIdSearch}
              onChange={(event) => setOrderIdSearch(event.target.value)}
              placeholder="Enter order ID, for example 1001"
              required
            />
          </label>
          <button type="submit" className="secondary-button">
            Find Order
          </button>
        </form>

        {errorMessage ? <p className="message error">{errorMessage}</p> : null}
        {successMessage ? <p className="message success">{successMessage}</p> : null}

        {loading ? (
          <p className="placeholder">Loading orders...</p>
        ) : orders.length === 0 ? (
          <p className="placeholder">No orders found for the selected filter.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Customer</th>
                  <th>Status</th>
                  <th>Total</th>
                  <th>Items</th>
                  <th>View</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td>{order.id}</td>
                    <td>{order.customerId}</td>
                    <td>
                      <span className={`status-pill status-${order.status.toLowerCase()}`}>
                        {order.status}
                      </span>
                    </td>
                    <td>${order.totalAmount.toFixed(2)}</td>
                    <td>{order.items.length}</td>
                    <td>
                      <Link to={`/orders/${order.id}`} className="table-link">
                        Details
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
