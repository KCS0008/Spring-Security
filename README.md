# Authentication Service

A secure authentication service built with Spring Boot that provides user registration, login, and password reset functionality with OTP verification.

## Features

- User Registration with email verification
- Secure Login with JWT authentication
- Password Reset with OTP verification
- Security Question/Answer verification
- Email notifications for OTP delivery
- Token-based authentication with JWT
- H2 in-memory database for development

## Technical Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- H2 Database
- JWT (JSON Web Tokens)
- JavaMail for email services
- Lombok for reducing boilerplate code

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- Gmail account (for sending OTP emails)

### Configuration

1. Clone the repository
2. Configure email settings in `application.properties`:
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-specific-password
   ```
   Note: For Gmail, you'll need to generate an App Password if you have 2FA enabled.

3. (Optional) Modify other properties as needed:
   - JWT secret and expiration
   - Server port
   - Database configuration

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123",
    "securityQuestion": "What is your favorite color?",
    "securityAnswer": "blue"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123"
}
```

#### Forgot Password
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
    "email": "user@example.com",
    "securityAnswer": "blue"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
    "email": "user@example.com",
    "otp": "123456",
    "newPassword": "newpassword123"
}
```

## Security Features

- Passwords are encrypted using BCrypt
- JWT tokens for stateless authentication
- OTP expiration for password reset security
- Security question verification for password reset
- Email-based OTP verification

## Development

### Database

The application uses H2 in-memory database for development. You can access the H2 console at:
```
http://localhost:8080/h2-console
```

Default credentials:
- JDBC URL: `jdbc:h2:mem:authdb`
- Username: `sa`
- Password: (empty)

### Email Configuration

The application uses Gmail SMTP for sending emails. Make sure to:
1. Enable "Less secure app access" or
2. Generate an App Password if using 2FA

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.