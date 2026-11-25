package example;

import example.checkout.CheckoutService;
import example.config.ServiceConfig;
import example.infrai.InfraiClient;

public final class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("usage: Main USER_ID ORDER_ID AMOUNT");
        ServiceConfig c = ServiceConfig.fromEnvironment();
        CheckoutService.OrderResult r = new CheckoutService(new InfraiClient(c.baseUrl(), c.apiKey()), c.experimentKey()).placeOrder(args[0], args[1], Double.parseDouble(args[2]));
        System.out.printf("{orderId:%s,userId:%s,bucket:%d,fulfillment:%s,receipt:%s}%n", r.orderId(), r.userId(), r.bucket(), r.fulfillment(), r.receipt());
    }
}
