# One-time book purchases

Only non-consumable, permanent book access is supported. Product IDs in API responses are existing numeric `books.id` values. Store IDs can differ across iOS and Android.

## Current integration status

The purchase pipeline, database persistence, catalog validation and authenticated endpoints are implemented. **Apple cryptographic verification is mocked**, as requested. Real StoreKit JWS values are rejected by the mock. Google verification and an Android submission endpoint are deferred.

`AppleTransactionDecoder` is the replacement point for a real Apple verifier: it must cryptographically verify the JWS before returning transaction data. Adding credentials alone does not turn the mock into real verification. A production adapter and its signature/certificate verification tests must be added before accepting real purchases.

No external store requests or credentials are used in this version.

## Endpoints

Both endpoints use the existing JWT bearer authentication; the JWT subject determines the user.

`POST /purchases/ios`

```json
{"signedTransaction":"dev-book-1-transaction-1"}
```

Success (200):

```json
{"productId":1,"granted":true}
```

The request accepts only the proof. Separate client user, product, transaction, status or price fields are rejected by the existing strict JSON configuration.

`GET /me/entitlements`

```json
{"products":[1]}
```

An account without access receives `{"products":[]}`. The result is the same regardless of the client's platform. `GET /books` now derives `isPurchased` from entitlements.

The former `POST /books/{id}/purchase` is removed (404). Historical `user_purchases` records remain, but are not copied to entitlements or used to authorize access, per the requested migration policy.

Existing lesson/homework endpoints retain their existing authorization behavior; this change adds purchase state and does not introduce content paywall checks on those endpoints.

## Development setup

Use a separate development database: mock grants are persisted, just like purchases.

1. Start the backend normally so Flyway applies V8 and books are seeded.
2. Add an explicit server-owned product mapping for an existing book (adjust 1 to your book ID):

```sql
INSERT INTO store_product_mappings (platform, store_product_id, product_id)
VALUES ('IOS', 'dev.book.1.ios', 1);
-- Optional future Android mapping to the same book:
INSERT INTO store_product_mappings (platform, store_product_id, product_id)
VALUES ('ANDROID', 'dev.book.1.android', 1);
```

3. Set environment variables and restart:

```powershell
$env:APP_ENV = 'development'
$env:PURCHASE_VERIFICATION_MODE = 'mock'
$env:APPLE_BUNDLE_ID = 'dev.books.app'
$env:APPLE_MOCK_FIXTURES_FILE = 'C:\work\backend\web_books\docs\examples\apple-purchase-fixtures.json'
.\gradlew.bat run
```

4. Authenticate as a test user and submit the opaque fixture key shown above. It can only be claimed by one backend account. Repeating it as that account succeeds. Use another server fixture with a distinct transaction ID for another test purchase.

Fixture content is server-owned; clients cannot fabricate transactions by encoding JSON as JWS. Do not expose a fixture editing endpoint. The sample token is intentionally public for local testing.

Defaults: `PURCHASE_VERIFICATION_MODE=disabled` returns 503 for submissions while entitlement retrieval continues working. Mock mode requires `APP_ENV=development` or `test`, the bundle ID and a nonempty fixture file. Only Sandbox fixtures are accepted in configured mock mode. A production environment with mock mode fails startup. No default catalog entries or automatic book grants are installed.

## Persistence and errors

- `store_product_mappings`: allow-list of store IDs to shared book IDs.
- `purchases`: verified transaction records, unique by `(platform, store_transaction_id)`.
- `entitlements`: authoritative access, unique by `(user_id, product_id)`.
- Purchase and entitlement inserts share one Exposed database transaction. Uniqueness constraints handle concurrency; failures roll back both writes.
- Same transaction/same account: idempotent 200. Different account: 409, without a new grant.
- Repeat transactions retain their original domain mapping even if the catalog changes.
- User deletion is restricted when purchase records exist, preserving transaction ownership against reassignment.
- Store environment is recorded. Test and production databases must stay separate.
- Revoked, non-purchased ownership, subscription and consumable proofs are rejected. Refund notifications/revocation synchronization are outside this implementation.

Expected errors retain the project's `ErrorResponse` envelope (`reason`, `statusCode`):

| HTTP | reason |
| --- | --- |
| 400 | INVALID_PROOF, WRONG_APPLICATION, UNSUPPORTED_STORE, UNSUPPORTED_ENVIRONMENT |
| 401 | Existing authentication errors |
| 409 | TRANSACTION_ALREADY_CLAIMED |
| 422 | UNKNOWN_PRODUCT |
| 503 | VERIFICATION_UNAVAILABLE |

Malformed JSON uses the existing bad-request envelope. Unexpected database/infrastructure exceptions follow existing 500 handling.

## Validation

Run `.\gradlew.bat test`. Tests use H2 PostgreSQL mode, execute the actual V8 SQL, and cover JWT isolation, duplicate/concurrent transactions, rollback, catalog validation, rejected fixture cases and shared Android/iOS domain IDs. They do not validate real Apple signatures or contact either store. PostgreSQL deployment/Flyway execution still needs verification in the target environment.
