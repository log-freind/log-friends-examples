import 'reflect-metadata';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const port = process.env.PORT ?? 8083;
  await app.listen(port);
  console.log(`Log Friends NestJS example running on port ${port}`);
}

bootstrap();
