export type ShopStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED';

export interface ShopRegistrationRequest {
  name:        string;
  regNumber:   string;
  ownerName:   string;
  phone?:      string;
  email?:      string;
  address?:    string;
  latitude?:   number;
  longitude?:  number;
}

export interface ShopResponse {
  id:            number;
  name:          string;
  regNumber:     string;
  ownerName:     string;
  phone:         string | null;
  email:         string | null;
  address:       string | null;
  latitude:      number | null;
  longitude:     number | null;
  status:        ShopStatus;
  createdAt:     string;
  averageRating: number | null;
  totalReviews:  number | null;
}