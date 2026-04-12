import request from '@/utils/request'
import type { Account, OrderSaveRequest } from '@/types'

export function getAccountByUserId(userId: number) {
  return request.get<{ data: Account[] }>(`/accounts/${userId}`)
}

export function createOrder(data: OrderSaveRequest) {
  return request.post('/orders', data)
}
