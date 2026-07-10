export type SubmissionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type PaymentType = 'CASH' | 'BANK_TRANSFER' | 'CARD' | 'CHEQUE';

export interface PaymentRequest {
  ticketId:         number;
  amount:           number;
  paymentType:      PaymentType;
  bankName?:        string;
  referenceNumber?: string;
  notes?:           string;
  recordedBy?:      number;
}

export interface PaymentResponse {
  id:               number;
  ticketId:         number;
  ticketNumber:     string;
  customerId:       number;
  customerName:     string;
  amount:           number;
  paymentType:      PaymentType;
  bankName:         string | null;
  referenceNumber:  string | null;
  notes:            string | null;
  recordedBy:       number | null;
  recordedByName:   string | null;
  createdAt:        string;
}

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