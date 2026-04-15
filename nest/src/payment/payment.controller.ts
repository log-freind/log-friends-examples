import {
  Controller,
  Post,
  Body,
  Param,
  Query,
} from '@nestjs/common';
import { PaymentService } from './payment.service';
import { CreatePaymentDto } from './dto/create-payment.dto';
import { PaymentResponseDto } from './dto/payment-response.dto';

@Controller('payments')
export class PaymentController {
  constructor(private readonly paymentService: PaymentService) {}

  @Post()
  processPayment(@Body() dto: CreatePaymentDto): PaymentResponseDto {
    return this.paymentService.processPayment(dto);
  }

  @Post(':txId/refund')
  refundPayment(
    @Param('txId') txId: string,
    @Query('reason') reason?: string,
  ): PaymentResponseDto {
    return this.paymentService.refundPayment(txId, reason);
  }
}
