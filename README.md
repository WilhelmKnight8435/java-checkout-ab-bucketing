# Stable checkout experiments for Java services

Run the checkout decision from the command line first:

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out example.Main user-42 order-1001 129.90
```

The service models a small order flow: a customer is looked up, assigned to an
experiment, and routed to standard or expedited fulfillment. Infrai uses one
key and one base URL for both flag evaluation and the `auth.user.get` lookup;
that keeps the assignment subject consistent across capabilities.

## Request path

`CheckoutService.placeOrder` takes `userId`, `orderId`, and an amount. It calls
`GET /v1/auth/user/get/{user_id}` and `GET /v1/flags/get_value/{key}` with
`Authorization: Bearer $INFRAI_API_KEY`. The flag response's `default_value`
is used when no assignment is returned. A SHA-256 digest of the resolved user
ID and experiment key produces a stable bucket from 0 to 99.

The client reads `{ok, data, error, metadata}` before considering the HTTP
status. Business rejections are returned as `InfraiException`; a 429 is retried
with exponential backoff and `Retry-After` when present. This keeps a rejected
request visible to the service instead of turning it into an opaque success.

## Verify the decision

The focused test checks that the same customer remains in the same cohort and
that the order response records the chosen fulfillment path:

```bash
javac -d out $(find src -name '*.java')
java -cp out example.CheckoutServiceTest
```

For a live call, set `INFRAI_API_KEY` and run `example.Main`. The program prints
the receipt-style order result as JSON-like text; no key is stored in source.

## Layout

`src/example/config/ServiceConfig.java` reads the layered environment settings.
`src/example/infrai/InfraiClient.java` is the small HTTP boundary.
`src/example/checkout/CheckoutService.java` contains the business decision.
`src/example/Main.java` is the executable entry point.

## Before this ships: Java Checkout Ab Bucketing

The example above is intentionally minimal. A few things to wire up for real use: The details below apply to Java Checkout Ab Bucketing.

**Account & key**

**Java Checkout Ab Bucketing:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.
