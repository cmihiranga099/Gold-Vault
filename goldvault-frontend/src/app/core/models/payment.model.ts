export type PaymentType = 'INTEREST' | 'PARTIAL' | 'FULL_REDEMPTION';
export type PaymentMethod = 'CASH' | 'CARD' | 'ONLINE_TRANSFER' | 'LANKAQR';

export interface PaymentRequest {
  ticketId: number;
  amount: number;
  paymentType: PaymentType;
  paymentMethod?: PaymentMethod;
  referenceNumber?: string;
  receivedBy?: number;
}

export interface PaymentResponse {
  id: number;
  ticketId: number;
  ticketNumber: string;
  amount: number;
  paymentType: PaymentType;
  paymentMethod: PaymentMethod;
  referenceNumber: string | null;
  paymentDate: string;
  receivedBy: number | null;
  remainingBalance: number;
  ticketRedeemed: boolean;
}

export interface DailyCollectionResponse {
  date: string;
  totalCollected: number;
  paymentCount: number;
  totalInterest: number;
  totalPartial: number;
  totalFullRedemption: number;
  payments: PaymentResponse[];
}