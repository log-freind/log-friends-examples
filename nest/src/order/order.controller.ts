import {
  Controller,
  Post,
  Delete,
  Body,
  Param,
  Query,
} from '@nestjs/common';
import { OrderService } from './order.service';
import { CreateOrderDto } from './dto/create-order.dto';
import { OrderResponseDto } from './dto/order-response.dto';

@Controller('orders')
export class OrderController {
  constructor(private readonly orderService: OrderService) {}

  @Post()
  createOrder(@Body() dto: CreateOrderDto): OrderResponseDto {
    return this.orderService.createOrder(dto);
  }

  @Delete(':orderId')
  cancelOrder(
    @Param('orderId') orderId: string,
    @Query('reason') reason?: string,
  ): OrderResponseDto {
    return this.orderService.cancelOrder(orderId, reason);
  }
}
