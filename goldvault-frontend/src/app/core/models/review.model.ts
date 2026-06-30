export interface ReviewRequest {
    ticketId: number;
    rating:   number;
    comment?: string;
  }
  
  export interface ReviewResponse {
    id:           number;
    shopId:       number;
    shopName:     string;
    customerId:   number;
    customerName: string;
    ticketId:     number;
    ticketNumber: string;
    rating:       number;
    comment:      string | null;
    status:       string;
    createdAt:    string;
  }
  
  export interface ShopRatingResponse {
    shopId:             number;
    shopName:           string;
    averageRating:      number;
    totalReviews:       number;
    ratingDistribution: Record<number, number>;
  }