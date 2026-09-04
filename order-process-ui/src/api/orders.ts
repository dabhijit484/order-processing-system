import { apiClient } from "./client";
import { CreateOrderPayload, Order, OrderStatus } from "../types/order";

export async function fetchOrders(status?: OrderStatus | ""): Promise<Order[]> {
  const response = await apiClient.get<Order[]>("/orders", {
    params: status ? { status } : {}
  });
  return response.data;
}

export async function fetchOrder(orderId: string): Promise<Order> {
  const response = await apiClient.get<Order>(`/orders/${orderId}`);
  return response.data;
}

export async function createOrder(payload: CreateOrderPayload): Promise<Order> {
  const response = await apiClient.post<Order>("/orders", payload);
  return response.data;
}

export async function cancelOrder(orderId: number): Promise<Order> {
  const response = await apiClient.post<Order>(`/orders/${orderId}/cancel`);
  return response.data;
}
