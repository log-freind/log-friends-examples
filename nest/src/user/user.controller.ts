import {
  Controller,
  Post,
  Put,
  Body,
  Param,
} from '@nestjs/common';
import { UserService } from './user.service';
import { CreateUserDto } from './dto/create-user.dto';
import { UserResponseDto } from './dto/user-response.dto';

@Controller('users')
export class UserController {
  constructor(private readonly userService: UserService) {}

  @Post()
  createUser(@Body() dto: CreateUserDto): UserResponseDto {
    return this.userService.createUser(dto);
  }

  @Put(':userId/deactivate')
  deactivateUser(@Param('userId') userId: string): UserResponseDto {
    return this.userService.deactivateUser(userId);
  }
}
