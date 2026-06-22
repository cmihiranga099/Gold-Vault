export interface CustomerRequest {
    fullName: string;
    nic: string;
    phone?: string;
    email?: string;
    address?: string;
    dob?: string;
    nicPhotoUrl?: string;
  }
  
  export interface CustomerResponse {
    id: number;
    fullName: string;
    nic: string;
    phone: string;
    email: string;
    address: string;
    dob: string;
    kycStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
    nicPhotoUrl: string | null;
    shopId: number;
    createdAt: string;
  }