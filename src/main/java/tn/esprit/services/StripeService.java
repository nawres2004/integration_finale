package tn.esprit.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class StripeService {

    private static final String SECRET_KEY = "sk_test_51TO1YBIHAI63LG0194RVFDGzo3uAc1LZJLYqsiuosriA8UHEaX4rzUSVgVXJjyuThB30du6fqDL5totWnBi2TOI8000EvJyAWy";

    static {
        Stripe.apiKey = SECRET_KEY;
    }

    // ──────────────────────────────────────────
    // Créer une session Checkout → retourne l'URL et l'ID
    // ──────────────────────────────────────────
    public String[] creerSessionCheckout(double montantDT, String description) throws StripeException {
        long montantCentimes = (long) (montantDT * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:9090/success")
                .setCancelUrl("http://localhost:9090/cancel")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(montantCentimes)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Don VitaPlus")
                                        .setDescription(description)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

        Session session = Session.create(params);
        System.out.println("✅ Session Stripe : " + session.getId());
        return new String[]{ session.getId(), session.getUrl() }; // [0]=id, [1]=url
    }
}
