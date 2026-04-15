import { Injectable } from '@nestjs/common';
import { CreatePaymentDto } from './dto/create-payment.dto';
import { PaymentResponseDto } from './dto/payment-response.dto';

@Injectable()
export class PaymentService {
  processPayment(dto: CreatePaymentDto): PaymentResponseDto {
    return {
      txId: `TX-${Date.now()}`,
      status: 'PROCESSED',
    };
  }

  refundPayment(txId: string, reason?: string): PaymentResponseDto {
    return {
      txId,
      status: 'REFUNDED',
      reason: reason ?? 'No reason provided',
    };
  }
}
