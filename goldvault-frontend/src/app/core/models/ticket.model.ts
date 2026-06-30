export type GoldType     = 'NECKLACE' | 'RING' | 'BANGLE' | 'EARRING' | 'CHAIN' | 'OTHER';
export type GoldPurity   = 'K24' | 'K22' | 'K21' | 'K18' | 'P916' | 'P750' | 'OTHER';
export type InterestType = 'FLAT' | 'REDUCING';
export type TicketStatus = 'ACTIVE' | 'REDEEMED' | 'EXPIRED' | 'AUCTIONED';

export interface GoldItemRequest {
  description:    string;
  goldType:       GoldType;
  weightGrams:    number;
  purity:         GoldPurity;
  estimatedValue?: number;
  photoUrl?:      string;
}

export interface GoldItemResponse {
  id:             number;
  description:    string;
  goldType:       GoldType;
  weightGrams:    number;
  purity:         GoldPurity;
  estimatedValue: number;
  photoUrl:       string | null;
}

export interface PawnTicketRequest {
  customerId:    number;
  branchId?:     number;
  loanAmount:    number;
  interestRate:  number;
  interestType?: InterestType;
  periodMonths:  number;
  notes?:        string;
  goldItems:     GoldItemRequest[];
}

export interface PawnTicketResponse {
  id:                  number;
  ticketNumber:        string;
  customerId:          number;
  customerName:        string;
  customerNic:         string;
  shopId:              number;
  branchId:            number | null;
  loanAmount:          number;
  interestRate:        number;
  interestType:        InterestType;
  pawnDate:            string;
  expiryDate:          string;
  status:              TicketStatus;
  qrCode:              string;
  notes:               string | null;
  goldItems:           GoldItemResponse[];
  totalPaid:           number;
  outstandingBalance:  number;
  daysElapsed:         number;
  overdue:             boolean;
  shopName: string;
  // Renewal fields
  renewalCount:        number;
  accruedInterestToday: number;
}