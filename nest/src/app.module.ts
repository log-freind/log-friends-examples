import { Module } from '@nestjs/common';
import { OrderModule } from './order/order.module';
import { PaymentModule } from './payment/payment.module';
import { UserModule } from './user/user.module';

@Module({
  imports: [OrderModule, PaymentModule, UserModule],
})
export class AppModule {}
