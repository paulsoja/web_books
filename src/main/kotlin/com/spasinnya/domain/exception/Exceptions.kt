package com.spasinnya.domain.exception

class InvalidOtpException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String = "User not found") : RuntimeException(message)

class UnauthorizedException(message: String? = "Unauthorized") : RuntimeException(message)
class ForbiddenException(message: String? = "Forbidden") : RuntimeException(message)
class NotFoundException(message: String? = "Not found") : RuntimeException(message)
class BadRequestException(message: String? = "Bad request") : RuntimeException(message)
class ConflictException(message: String? = "Conflict") : RuntimeException(message)
class UnprocessableEntityException(message: String? = "Unprocessable entity") : RuntimeException(message)
class TooManyRequestsException(message: String? = "Too many requests") : RuntimeException(message)
