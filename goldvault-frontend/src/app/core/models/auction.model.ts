import { GoldItemResponse } from './ticket.model';

export interface PlaceBidRequest {
  bidderName:  string;
  bidderPhone: string;
  amount:      number;
}

export interface AuctionResponse {
  id:                 number;
  ticketId:           number;
  ticketNumber:       string;
  shopId:             number;
  shopName:           string;
  startingPrice:      number;
  currentBid:         number | null;
  bidCount:           number;
  status:             'OPEN' | 'CLOSED' | 'CANCELLED';
  startsAt:           string;
  endsAt:             string;
  ended:              boolean;
  goldItems:          GoldItemResponse[];
  winningBidderName:  string | null;
}

export interface AuctionBidResponse {
  id:         number;
  bidderName: string;
  amount:     number;
  createdAt:  string;
}