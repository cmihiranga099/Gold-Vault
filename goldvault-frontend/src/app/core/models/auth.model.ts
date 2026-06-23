export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
  nic: string;
  shopId: number;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  fullName: string;
  role: 'ROLE_ADMIN' | 'ROLE_SHOP_ADMIN' | 'ROLE_STAFF' | 'ROLE_CUSTOMER';
  shopId: number | null;
  customerId: number | null;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}