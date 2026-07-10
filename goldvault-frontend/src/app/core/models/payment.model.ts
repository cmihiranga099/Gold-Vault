export type PaymentType   = 'INTEREST' | 'PARTIAL' | 'FULL_REDEMPTION' | 'RENEWAL';
export type PaymentMethod = 'CASH' | 'CARD' | 'ONLINE_TRANSFER' | 'LANKAQR';

export interface PaymentRequest {
  ticketId:        number;
  amount:          number;
  paymentType:     PaymentType;
  paymentMethod?:  PaymentMethod;
  referenceNumber?: string;
  receivedBy?:     number;
}

export interface PaymentResponse {
  id:               number;
  ticketId:         number;
  ticketNumber:     string;
  amount:           number;
  paymentType:      PaymentType;
  paymentMethod:    PaymentMethod;
  referenceNumber:  string | null;
  paymentDate:      string;
  receivedBy:       number | null;
  remainingBalance: number;
  ticketRedeemed:   boolean;
}

export interface DailyCollectionResponse {
  date:                string;
  totalCollected:      number;
  paymentCount:        number;
  totalInterest:       number;
  totalPartial:        number;
  totalFullRedemption: number;
  payments:            PaymentResponse[];
}

export type SubmissionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface PaymentSubmissionResponse {
  id:                  number;
  ticketId:            number;
  ticketNumber:        string;
  shopName:            string;
  customerId:          number;
  customerName:        string;
  amount:              number;
  paymentType:         PaymentType;
  bankName:            string | null;
  referenceNumber:     string;
  receiptUrl:          string;
  status:              SubmissionStatus;
  rejectionReason:     string | null;
  resultingPaymentId:  number | null;
  submittedAt:         string;
  reviewedAt:          string | null;
}