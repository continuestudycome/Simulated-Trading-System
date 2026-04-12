export interface Account {
  id: number
  userId: number
  balance: number
  frozen: number
  version: number
}

export interface Order {
  id: number
  userId: number
  stockCode: string
  type: number
  price: number
  quantity: number
  status: number
  createTime: string
}

export interface Position {
  id: number
  userId: number
  stockCode: string
  quantity: number
  costPrice: number
}

export interface OrderSaveRequest {
  userId: number
  stockCode: string
  type: number
  price: number
  quantity: number
  status: number
  createTime: string
}
