import { Injectable } from '@nestjs/common';
import { CreateOrderDto } from './dto/create-order.dto';
import { OrderResponseDto } from './dto/order-response.dto';

@Injectable()
export class OrderService {
  createOrder(dto: CreateOrderDto): OrderResponseDto {
    return {
      orderId: `ORD-${Date.now()}`,
      status: 'CREATED',
    };
  }

  cancelOrder(orderId: string, reason?: string): OrderResponseDto {
    return {
      orderId,
      status: 'CANCELLED',
      reason: reason ?? 'No reason provided',
    };
  }
}
