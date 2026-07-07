export interface ChatTurn {
    role: 'user' | 'assistant';
    content: string;
  }
  
  export interface ChatRequest {
    message: string;
    history: ChatTurn[];
    customerId?: number | null;
    shopId?: number | null;
    portal: 'PUBLIC' | 'CUSTOMER' | 'SHOP' | 'ADMIN';
  }
  
  export interface SuggestedLink {
    label: string;
    url: string;
  }
  
  export interface ChatResponse {
    reply: string;
    source: 'RULE' | 'AI';
    links: SuggestedLink[] | null;
  }