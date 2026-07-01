export type PromoType = 'REDUCED_INTEREST' | 'BONUS_POINTS' | 'FREE_RENEWAL' | 'CUSTOM';

export interface PromotionRequest {
  title:       string;
  description?: string;
  promoType:   PromoType;
  promoValue?: number;
  startsAt:    string;
  endsAt:      string;
}

export interface PromotionResponse {
  id:             number;
  shopId:         number;
  shopName:       string;
  title:          string;
  description:    string | null;
  promoType:      PromoType;
  promoValue:     number | null;
  startsAt:       string;
  endsAt:         string;
  status:         'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  currentlyActive: boolean;
  daysRemaining:  number;
}