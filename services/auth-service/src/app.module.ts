import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";
import { JwtModule } from "@nestjs/jwt";
import { PassportModule } from "@nestjs/passport";
import { ConfigModule } from "@nestjs/config";
import { AuthModule } from "./auth/auth.module";
import { User } from "./auth/entities/user.entity";
import { HealthController } from "./health/health.controller";

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    TypeOrmModule.forRoot({
      type: "postgres",
      url:
        process.env.DATABASE_URL ||
        "postgresql://quantis:password@localhost:5432/quantis_trading",
      entities: [User],
      synchronize: true, // Only for development
      logging: false,
    }),
    PassportModule.register({ defaultStrategy: "jwt" }),
    JwtModule.register({
      secret:
        process.env.JWT_SECRET ||
        "your-super-secret-jwt-key-change-in-production",
      signOptions: { expiresIn: "24h" },
    }),
    AuthModule,
  ],
  controllers: [HealthController],
})
export class AppModule {}
