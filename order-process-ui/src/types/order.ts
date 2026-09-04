export type OrderStatus =
  | "PENDING"
  | "PROCESSING"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED";

export interface OrderItem {
  id?: number;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal?: number;
}

export interface Order {
  id: number;
  customerId: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  version: number;
  items: OrderItem[];
}

export interface CreateOrderPayload {
  customerId: string;
  items: Array<{
    productId: string;
    productName: string;
    quantity: number;
    unitPrice: number;
  }>;
}
