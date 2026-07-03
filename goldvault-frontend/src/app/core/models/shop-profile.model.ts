import { ReviewResponse } from './review.model';
import { PromotionResponse } from './promotion.model';

export interface ShopProfileResponse {
  id:                 number;
  name:               string;
  ownerName:          string;
  phone:              string | null;
  email:              string | null;
  address:            string | null;
  latitude:           number | null;
  longitude:          number | null;
  status:             string;
  goldRates:          Record<string, number>;
  averageRating:      number;
  totalReviews:       number;
  ratingDistribution: Record<number, number>;
  recentReviews:      ReviewResponse[];
  activePromotions:   PromotionResponse[];
  totalTicketsEver:   number;
  activeTickets:      number;
}