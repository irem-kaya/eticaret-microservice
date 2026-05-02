package com.eticaret.payment.strategy;

import com.eticaret.payment.dto.PaymentRequest;
import com.eticaret.payment.dto.PaymentResult;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.CreateCancelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("iyzico")
public class IyzicoPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(IyzicoPaymentStrategy.class);

    @Value("${app.payment.iyzico.api-key}")
    private String apiKey;

    @Value("${app.payment.iyzico.secret-key}")
    private String secretKey;

    @Value("${app.payment.iyzico.base-url:https://sandbox-api.iyzipay.com}")
    private String baseUrl;

    private Options buildOptions() {
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);
        return options;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        log.info("Iyzico ödeme başlatılıyor: orderId={}, amount={}", request.orderId(), request.amount());

        try {
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
            paymentRequest.setLocale(Locale.TR.getValue());
            paymentRequest.setConversationId(request.orderId());
            paymentRequest.setPrice(request.amount().setScale(2, RoundingMode.HALF_UP));
            paymentRequest.setPaidPrice(request.amount().setScale(2, RoundingMode.HALF_UP));
            paymentRequest.setCurrency(Currency.TRY.name());
            paymentRequest.setInstallment(1);
            paymentRequest.setBasketId(request.orderId());
            paymentRequest.setPaymentChannel(PaymentChannel.WEB.name());
            paymentRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

            // Kart bilgileri
            PaymentCard paymentCard = new PaymentCard();
            paymentCard.setCardHolderName(request.cardHolderName());
            paymentCard.setCardNumber(request.cardNumber());
            paymentCard.setExpireMonth(request.expireMonth());
            paymentCard.setExpireYear(request.expireYear());
            paymentCard.setCvc(request.cvc());
            paymentCard.setRegisterCard(0);
            paymentRequest.setPaymentCard(paymentCard);

            // Alıcı bilgileri
            Buyer buyer = new Buyer();
            buyer.setId(request.userId());
            buyer.setName("TMarket");
            buyer.setSurname("Kullanıcı");
            buyer.setEmail("musteri@tmarket.com");
            buyer.setIdentityNumber("74300864791");
            buyer.setRegistrationAddress("Türkiye");
            buyer.setIp("85.34.78.112");
            buyer.setCity("Istanbul");
            buyer.setCountry("Turkey");
            paymentRequest.setBuyer(buyer);

            // Adres
            Address shippingAddress = new Address();
            shippingAddress.setContactName("TMarket Kullanıcı");
            shippingAddress.setCity("Istanbul");
            shippingAddress.setCountry("Turkey");
            shippingAddress.setAddress("Türkiye");
            paymentRequest.setShippingAddress(shippingAddress);
            paymentRequest.setBillingAddress(shippingAddress);

            // Sepet ürünleri
            List<BasketItem> basketItems = new ArrayList<>();
            BasketItem basketItem = new BasketItem();
            basketItem.setId(request.orderId());
            basketItem.setName("Sipariş #" + request.orderId());
            basketItem.setCategory1("Genel");
            basketItem.setItemType(BasketItemType.PHYSICAL.name());
            basketItem.setPrice(request.amount().setScale(2, RoundingMode.HALF_UP));
            basketItems.add(basketItem);
            paymentRequest.setBasketItems(basketItems);

            // Ödemeyi gerçekleştir
            Payment payment = Payment.create(paymentRequest, buildOptions());

            if ("success".equals(payment.getStatus())) {
                log.info("✅ Iyzico ödeme başarılı: paymentId={}", payment.getPaymentId());
                return new PaymentResult(
                        true,
                        payment.getPaymentId(),
                        request.orderId(),
                        request.amount(),
                        "Ödeme başarıyla tamamlandı",
                        "IYZICO",
                        LocalDateTime.now()
                );
            } else {
                log.warn("❌ Iyzico ödeme başarısız: {}", payment.getErrorMessage());
                return new PaymentResult(
                        false,
                        null,
                        request.orderId(),
                        request.amount(),
                        payment.getErrorMessage(),
                        "IYZICO",
                        LocalDateTime.now()
                );
            }

        } catch (Exception e) {
            log.error("💥 Iyzico ödeme hatası: {}", e.getMessage());
            return new PaymentResult(
                    false, null, request.orderId(),
                    request.amount(), "Ödeme servisi hatası: " + e.getMessage(),
                    "IYZICO", LocalDateTime.now()
            );
        }
    }

    @Override
    public PaymentResult refund(String paymentId, String orderId) {
        log.info("Iyzico iade başlatılıyor: paymentId={}", paymentId);

        try {
            CreateCancelRequest cancelRequest = new CreateCancelRequest();
            cancelRequest.setLocale(Locale.TR.getValue());
            cancelRequest.setConversationId(UUID.randomUUID().toString());
            cancelRequest.setPaymentId(paymentId);

            Cancel cancel = Cancel.create(cancelRequest, buildOptions());

            if ("success".equals(cancel.getStatus())) {
                log.info("✅ Iyzico iade başarılı: paymentId={}", paymentId);
                return new PaymentResult(
                        true, paymentId, orderId,
                        null, "İade başarıyla tamamlandı",
                        "IYZICO", LocalDateTime.now()
                );
            } else {
                log.warn("❌ Iyzico iade başarısız: {}", cancel.getErrorMessage());
                return new PaymentResult(
                        false, paymentId, orderId,
                        null, cancel.getErrorMessage(),
                        "IYZICO", LocalDateTime.now()
                );
            }

        } catch (Exception e) {
            log.error("💥 Iyzico iade hatası: {}", e.getMessage());
            return new PaymentResult(
                    false, paymentId, orderId,
                    null, "İade servisi hatası: " + e.getMessage(),
                    "IYZICO", LocalDateTime.now()
            );
        }
    }

    @Override
    public String getProviderName() {
        return "IYZICO";
    }
}