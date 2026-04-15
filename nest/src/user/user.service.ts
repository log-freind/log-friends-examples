import { Injectable } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UserResponseDto } from './dto/user-response.dto';

@Injectable()
export class UserService {
  createUser(dto: CreateUserDto): UserResponseDto {
    return {
      userId: `USR-${Date.now()}`,
      status: 'ACTIVE',
    };
  }

  deactivateUser(userId: string): UserResponseDto {
    return {
      userId,
      status: 'INACTIVE',
    };
  }
}
