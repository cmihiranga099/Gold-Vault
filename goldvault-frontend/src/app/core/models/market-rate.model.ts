export const PURITIES = ['K24', 'K22', 'K21', 'K18', 'P916', 'P750'] as const;
export type Purity = typeof PURITIES[number];

export interface MarketRateResponse {
  id:            number;
  purity:        string;
  ratePerGram:   number;
  source:        string;
  effectiveDate: string;
}

export interface ShopRateEntry {
  shopId:      number;
  shopName:    string;
  ratePerGram: number;
  aboveMarket: boolean;
  diffPercent: number;
}

export interface RateComparisonResponse {
  purity:      string;
  marketRate:  number | null;
  shopRates:   ShopRateEntry[];
}