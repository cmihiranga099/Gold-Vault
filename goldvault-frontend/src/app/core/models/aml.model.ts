export type AmlFlagType   = 'LARGE_TRANSACTION' | 'HIGH_VOLUME' | 'RAPID_CYCLING' | 'MULTIPLE_SHOPS';
export type AmlFlagStatus = 'OPEN' | 'REVIEWED' | 'DISMISSED';

export interface AmlFlagResponse {
  id:            number;
  customerId:    number;
  customerName:  string;
  customerNic:   string;
  shopId:        number;
  shopName:      string;
  ticketId:      number | null;
  ticketNumber:  string | null;
  flagType:      AmlFlagType;
  flagTypeLabel: string;
  description:   string;
  amount:        number | null;
  status:        AmlFlagStatus;
  reviewedBy:    string | null;
  reviewedAt:    string | null;
  reviewNote:    string | null;
  createdAt:     string;
}

export interface AmlSummary {
  openFlags:      number;
  reviewedFlags:  number;
  dismissedFlags: number;
  byType:         Record<string, number>;
}

export interface AmlReviewRequest {
  status:     'REVIEWED' | 'DISMISSED';
  reviewNote?: string;
  reviewedBy: string;
}