import { Link, Route, Routes } from "react-router-dom";
import { OrdersPage } from "./pages/OrdersPage";
import { OrderDetailPage } from "./pages/OrderDetailPage";

export default function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Order Operations Console</p>
          <h1>Order Processing System</h1>
          <p className="subtitle">
            Create orders, track lifecycle status, and manage pending cancellations.
          </p>
        </div>
        <nav>
          <Link to="/" className="nav-link">
            Dashboard
          </Link>
        </nav>
      </header>

      <main className="app-content">
        <Routes>
          <Route path="/" element={<OrdersPage />} />
          <Route path="/orders/:orderId" element={<OrderDetailPage />} />
        </Routes>
      </main>
    </div>
  );
}
