package com.questlog.backend.service.impl;

import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.dto.PaymentIntentResponse;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.service.PremiumService;
import com.questlog.backend.service.UserService;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PremiumServiceImpl implements PremiumService {

    private final UserService userService;
    private final StripeClient stripeClient;
    private final String successUrlPattern;
    private final String cancelUrlPattern;
    private final String publishableKey;

    public PremiumServiceImpl(
            UserService userService,
            StripeClient stripeClient,
            @Value("${stripe.success-url}") String successUrlPattern,
            @Value("${stripe.cancel-url}") String cancelUrlPattern,
            @Value("${stripe.publishable-key}") String publishableKey) {
        this.userService = userService;
        this.stripeClient = stripeClient;
        this.successUrlPattern = successUrlPattern;
        this.cancelUrlPattern = cancelUrlPattern;
        this.publishableKey = publishableKey;
    }

    @Override
    public CheckoutSessionResponse createCheckoutSession(Long userId) {
        log.info("Membuat sesi pembayaran premium Stripe untuk user ID: {}", userId);
        // Memanggil getUserById untuk memvalidasi keberadaan user
        userService.getUserById(userId);

        // Gantilah placeholder {USER_ID} dengan ID user sebenarnya
        String successUrl = successUrlPattern.replace("{USER_ID}", String.valueOf(userId));
        String cancelUrl = cancelUrlPattern.replace("{USER_ID}", String.valueOf(userId));

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(1500L) // $15.00
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("QuestLog Premium - Pro Adventurer Pack")
                                                                    .setDescription("Buka fitur premium QuestLog secara permanen.")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("userId", String.valueOf(userId))
                    .build();

            Session session = stripeClient.v1().checkout().sessions().create(params);
            
            log.info("Sesi pembayaran premium Stripe berhasil dibuat. URL Checkout: {}", session.getUrl());
            return new CheckoutSessionResponse(session.getUrl(), "SUCCESS");
        } catch (StripeException e) {
            log.error("Gagal membuat sesi Stripe Checkout untuk user ID: {}", userId, e);
            throw new RuntimeException("Gagal membuat sesi pembayaran Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public UserResponse handleWebhookPaymentSuccess(Long userId) {
        log.info("Menerima notifikasi pembayaran sukses untuk user ID: {}. Mengaktifkan fitur premium.", userId);
        return userService.setPremium(userId, true);
    }

    @Override
    @Transactional
    public String getSuccessPage(String sessionId, Long userId) {
        log.info("Memproses redirect sukses pembayaran untuk user ID: {}, session ID: {}", userId, sessionId);
        try {
            Session session = stripeClient.v1().checkout().sessions().retrieve(sessionId);
            
            if ("paid".equals(session.getPaymentStatus())) {
                log.info("Verifikasi pembayaran sukses dari Stripe untuk user ID: {}", userId);
                handleWebhookPaymentSuccess(userId);
                return getSuccessHtmlPage(userId);
            } else {
                log.warn("Mencoba mengakses halaman sukses tetapi status pembayaran belum lunas: {}", session.getPaymentStatus());
                return getFailureHtmlPage("Pembayaran Anda belum diselesaikan oleh Stripe.");
            }
        } catch (StripeException e) {
            log.error("Gagal mengambil data sesi Stripe untuk verifikasi", e);
            return getFailureHtmlPage("Gagal memverifikasi transaksi dengan Stripe: " + e.getMessage());
        }
    }

    @Override
    public String getCancelPage(Long userId) {
        log.info("User ID: {} membatalkan pembayaran.", userId);
        return getFailureHtmlPage("Pembayaran dibatalkan oleh pengguna.");
    }

    @Override
    public PaymentIntentResponse createPaymentIntent(Long userId) {
        log.info("Membuat Stripe PaymentIntent untuk user ID: {}", userId);
        userService.getUserById(userId);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(1500L) // $15.00
                    .setCurrency("usd")
                    .putMetadata("userId", String.valueOf(userId))
                    .build();

            PaymentIntent paymentIntent = stripeClient.v1().paymentIntents().create(params);
            log.info("Stripe PaymentIntent berhasil dibuat untuk user ID: {}. ID: {}", userId, paymentIntent.getId());

            return new PaymentIntentResponse(paymentIntent.getClientSecret(), publishableKey, "SUCCESS");
        } catch (StripeException e) {
            log.error("Gagal membuat Stripe PaymentIntent untuk user ID: {}", userId, e);
            throw new RuntimeException("Gagal membuat PaymentIntent Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public UserResponse confirmPayment(Long userId, String paymentIntentId) {
        log.info("Mengonfirmasi status Stripe PaymentIntent ID: {} untuk user ID: {}", paymentIntentId, userId);
        try {
            PaymentIntent paymentIntent = stripeClient.v1().paymentIntents().retrieve(paymentIntentId);
            
            if ("succeeded".equals(paymentIntent.getStatus())) {
                log.info("Verifikasi PaymentIntent berhasil dari Stripe untuk user ID: {}", userId);
                return handleWebhookPaymentSuccess(userId);
            } else {
                log.warn("Verifikasi PaymentIntent gagal karena status belum berhasil: {}", paymentIntent.getStatus());
                throw new RuntimeException("Status transaksi Stripe belum berhasil: " + paymentIntent.getStatus());
            }
        } catch (StripeException e) {
            log.error("Gagal memverifikasi PaymentIntent dari Stripe", e);
            throw new RuntimeException("Gagal memverifikasi transaksi Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    public String getMockCheckoutPage(Long userId) {
        log.info("Merender halaman mock checkout untuk user ID: {}", userId);
        return """
        <!DOCTYPE html>
        <html lang="id">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Stripe Crypto Checkout (Mock)</title>
            <style>
                body {
                    background-color: #121214;
                    color: #ffffff;
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                }
                .checkout-card {
                    background: linear-gradient(135deg, #1f1f23 0%, #151518 100%);
                    border: 1px solid #3a3a42;
                    border-radius: 16px;
                    padding: 32px;
                    text-align: center;
                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
                    max-width: 400px;
                    width: 100%;
                }
                h2 {
                    color: #625afa; /* Stripe purple */
                    margin-bottom: 8px;
                }
                p {
                    color: #a0a0b0;
                    font-size: 14px;
                    line-height: 1.6;
                    margin-bottom: 24px;
                }
                .price {
                    font-size: 32px;
                    font-weight: bold;
                    color: #fff;
                    margin-bottom: 24px;
                }
                .price span {
                    font-size: 16px;
                    color: #00d4b2; /* Crypto green */
                }
                .btn-pay {
                    background: linear-gradient(90deg, #625afa 0%, #00d4b2 100%);
                    color: white;
                    border: none;
                    border-radius: 8px;
                    padding: 14px 28px;
                    font-size: 16px;
                    font-weight: bold;
                    cursor: pointer;
                    width: 100%;
                    transition: transform 0.2s, box-shadow 0.2s;
                }
                .btn-pay:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 5px 15px rgba(98, 90, 250, 0.4);
                }
                .badge {
                    background-color: #2e2e38;
                    color: #00d4b2;
                    padding: 4px 12px;
                    border-radius: 20px;
                    font-size: 12px;
                    display: inline-block;
                    margin-bottom: 16px;
                }
            </style>
        </head>
        <body>
            <div class="checkout-card">
                <span class="badge">Stripe Crypto Integration</span>
                <h2>Pro Adventurer Pack</h2>
                <p>Buka fitur penjadwalan latihan intensif otomatis & analisis diet daging tingkat lanjut secara permanen.</p>
                <div class="price">15.00 <span>USDC (Crypto)</span></div>
                <form action="/api/v1/premium/mock-success" method="get">
                    <input type="hidden" name="userId" value="[USER_ID]">
                    <button type="submit" class="btn-pay">Bayar via Stripe Crypto</button>
                </form>
            </div>
        </body>
        </html>
        """.replace("[USER_ID]", String.valueOf(userId));
    }

    @Override
    public String getMockSuccessPage(Long userId) {
        log.info("Memproses webhook sukses pembayaran premium untuk user ID: {}", userId);
        handleWebhookPaymentSuccess(userId);
        return getSuccessHtmlPage(userId);
    }

    private String getSuccessHtmlPage(Long userId) {
        return """
        <!DOCTYPE html>
        <html lang="id">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Pembayaran Berhasil</title>
            <style>
                body {
                    background-color: #121214;
                    color: #ffffff;
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                }
                .success-card {
                    background: linear-gradient(135deg, #1f1f23 0%, #151518 100%);
                    border: 1px solid #2e7d32;
                    border-radius: 16px;
                    padding: 32px;
                    text-align: center;
                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
                    max-width: 400px;
                    width: 100%;
                }
                .icon {
                    font-size: 64px;
                    color: #4caf50;
                    margin-bottom: 16px;
                }
                h2 {
                    color: #4caf50;
                    margin-bottom: 8px;
                }
                p {
                    color: #a0a0b0;
                    font-size: 14px;
                    line-height: 1.6;
                    margin-bottom: 24px;
                }
                .back-info {
                    font-size: 12px;
                    color: #625afa;
                }
            </style>
        </head>
        <body>
            <div class="success-card">
                <div class="icon">✔</div>
                <h2>Transaksi Berhasil!</h2>
                <p>Status akun Anda telah berhasil diperbarui menjadi <strong>Premium Adventurer</strong> di database.</p>
                <p class="back-info">Anda sekarang dapat kembali ke aplikasi QuestLog.</p>
            </div>
        </body>
        </html>
        """;
    }

    private String getFailureHtmlPage(String reason) {
        return """
        <!DOCTYPE html>
        <html lang="id">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Pembayaran Gagal / Batal</title>
            <style>
                body {
                    background-color: #121214;
                    color: #ffffff;
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                }
                .failure-card {
                    background: linear-gradient(135deg, #1f1f23 0%, #151518 100%);
                    border: 1px solid #c62828;
                    border-radius: 16px;
                    padding: 32px;
                    text-align: center;
                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
                    max-width: 400px;
                    width: 100%;
                }
                .icon {
                    font-size: 64px;
                    color: #f44336;
                    margin-bottom: 16px;
                }
                h2 {
                    color: #f44336;
                    margin-bottom: 8px;
                }
                p {
                    color: #a0a0b0;
                    font-size: 14px;
                    line-height: 1.6;
                    margin-bottom: 24px;
                }
                .back-info {
                    font-size: 12px;
                    color: #625afa;
                }
            </style>
        </head>
        <body>
            <div class="failure-card">
                <div class="icon">✘</div>
                <h2>Transaksi Gagal / Dibatalkan</h2>
                <p>[REASON]</p>
                <p class="back-info">Anda dapat kembali ke aplikasi QuestLog untuk mencoba lagi.</p>
            </div>
        </body>
        </html>
        """.replace("[REASON]", reason);
    }
}
