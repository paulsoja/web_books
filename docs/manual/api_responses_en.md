
# API Responses Documentation

## POST /register

| Scenario                                                                                                 |                    HTTP Status | Response Body                                                                         | Notes                                                    |
|----------------------------------------------------------------------------------------------------------|-------------------------------:|---------------------------------------------------------------------------------------|----------------------------------------------------------|
| Valid request (new email, pending, confirmed, rate-limited, invalid email/password — always same output) |                     **200 OK** | ```json { "message": "If this email is not registered, you will receive an OTP" } ``` | Universal response, does not reveal whether email exists |
| Invalid JSON / missing fields                                                                            |            **400 Bad Request** | ```json { "error": "Invalid request payload" } ```                                    | Transport error                                          |
| Content-Type not application/json                                                                        | **415 Unsupported Media Type** | ```json { "error": "Content-Type must be application/json" } ```                      |                                                          |
| (Optional) exceeded rate-limit                                                                           |      **429 Too Many Requests** | ```json { "error": "Too many requests, please try again later" } ```                  | Can be hidden behind 200 OK                              |
| Internal error                                                                                           |  **500 Internal Server Error** | ```json { "error": "Internal server error" } ```                                      |                                                          |
| Dependent services unavailable                                                                           |    **503 Service Unavailable** | ```json { "error": "Service temporarily unavailable" } ```                            |                                                          |

---

## POST /verify-otp

| Scenario                          |                    HTTP Status | Response Body                                                    | Notes                                 |
|-----------------------------------|-------------------------------:|------------------------------------------------------------------|---------------------------------------|
| Code is valid                     |                     **200 OK** | ```json { "message": "Verified" } ```                            | May also return tokens if flow allows |
| Invalid JSON                      |            **400 Bad Request** | ```json { "error": "Invalid request payload" } ```               |                                       |
| Invalid/expired OTP               |            **400 Bad Request** | ```json { "error": "Invalid or expired OTP" } ```                | Do not expose email existence         |
| Too many attempts                 |      **429 Too Many Requests** | ```json { "error": "Too many attempts" } ```                     |                                       |
| Content-Type not application/json | **415 Unsupported Media Type** | ```json { "error": "Content-Type must be application/json" } ``` |                                       |
| Internal error                    |  **500 Internal Server Error** | ```json { "error": "Internal server error" } ```                 |                                       |
| Services unavailable              |    **503 Service Unavailable** | ```json { "error": "Service temporarily unavailable" } ```       |                                       |

---

## POST /login

| Scenario                          |                    HTTP Status | Response Body                                                        | Notes                                                |
|-----------------------------------|-------------------------------:|----------------------------------------------------------------------|------------------------------------------------------|
| Successful login                  |                     **200 OK** | ```json { "accessToken": "...", "refreshToken": "..." } ```          |                                                      |
| Invalid JSON                      |            **400 Bad Request** | ```json { "error": "Invalid request payload" } ```                   |                                                      |
| Invalid credentials               |           **401 Unauthorized** | ```json { "error": "Invalid credentials" } ```                       | Do not distinguish between email and password errors |
| Email not verified                |              **403 Forbidden** | ```json { "error": "Email not verified" } ```                        | Alternatively always return 401                      |
| Account locked                    |                 **423 Locked** | ```json { "error": "Account locked. Try later" } ```                 | Optional                                             |
| Rate-limit                        |      **429 Too Many Requests** | ```json { "error": "Too many requests, please try again later" } ``` |                                                      |
| Content-Type not application/json | **415 Unsupported Media Type** | ```json { "error": "Content-Type must be application/json" } ```     |                                                      |
| Internal error                    |  **500 Internal Server Error** | ```json { "error": "Internal server error" } ```                     |                                                      |
| Services unavailable              |    **503 Service Unavailable** | ```json { "error": "Service temporarily unavailable" } ```           |                                                      |

---

## POST /reset-password

| Scenario                          |                    HTTP Status | Response Body                                                                          | Notes                       |
|-----------------------------------|-------------------------------:|----------------------------------------------------------------------------------------|-----------------------------|
| Valid request (any email)         |                     **200 OK** | ```json { "message": "If this email exists, you will receive reset instructions" } ``` | Universal response          |
| Invalid JSON                      |            **400 Bad Request** | ```json { "error": "Invalid request payload" } ```                                     |                             |
| Content-Type not application/json | **415 Unsupported Media Type** | ```json { "error": "Content-Type must be application/json" } ```                       |                             |
| Rate-limit (optional)             |      **429 Too Many Requests** | ```json { "error": "Too many requests, please try again later" } ```                   | Can be hidden behind 200 OK |
| Internal error                    |  **500 Internal Server Error** | ```json { "error": "Internal server error" } ```                                       |                             |
| Services unavailable              |    **503 Service Unavailable** | ```json { "error": "Service temporarily unavailable" } ```                             |                             |

---

## POST /set-new-password

| Scenario                          |                    HTTP Status | Response Body                                                    | Notes                         |
|-----------------------------------|-------------------------------:|------------------------------------------------------------------|-------------------------------|
| Password updated successfully     |                     **200 OK** | ```json { "message": "Password updated" } ```                    | May also return tokens        |
| Invalid JSON                      |            **400 Bad Request** | ```json { "error": "Invalid request payload" } ```               |                               |
| Weak password                     |            **400 Bad Request** | ```json { "error": "Weak password" } ```                         | Or 422                        |
| Invalid/expired reset token       |           **401 Unauthorized** | ```json { "error": "Invalid or expired token" } ```              |                               |
| Password reuse (optional)         |               **409 Conflict** | ```json { "error": "Password reuse not allowed" } ```            | If enforcing password history |
| Rate-limit                        |      **429 Too Many Requests** | ```json { "error": "Too many attempts" } ```                     |                               |
| Content-Type not application/json | **415 Unsupported Media Type** | ```json { "error": "Content-Type must be application/json" } ``` |                               |
| Internal error                    |  **500 Internal Server Error** | ```json { "error": "Internal server error" } ```                 |                               |
| Services unavailable              |    **503 Service Unavailable** | ```json { "error": "Service temporarily unavailable" } ```       |                               |
