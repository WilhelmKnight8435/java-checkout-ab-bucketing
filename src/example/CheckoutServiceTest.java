package example;

import example.checkout.CheckoutService;

public final class CheckoutServiceTest {
    public static void main(String[] args) throws Exception {
        int first = CheckoutService.bucket("user-42", "checkout-fulfillment-v1");
        int second = CheckoutService.bucket("user-42", "checkout-fulfillment-v1");
        if (first != second || first < 0 || first > 99) throw new AssertionError("bucket is not stable");
        System.out.println("stable cohort=" + first);
    }
}
