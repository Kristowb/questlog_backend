package com.questlog.backend.controller;

import com.questlog.backend.dto.CheckoutRequest;
import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.dto.ConfirmPaymentRequest;
import com.questlog.backend.dto.PaymentIntentRequest;
import com.questlog.backend.dto.PaymentIntentResponse;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.service.PremiumService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/premium")
@Slf4j
@Tag(name = "Premium API", description = "Manajemen fitur akun premium, integrasi Stripe Checkout, dan Webhook")
public class PremiumController {

    private final PremiumService premiumService;
    private final String webhookSecret;

    public PremiumController(
            PremiumService premiumService,
            @Value("${stripe.webhook.secret:}") String webhookSecret) {
        this.premiumService = premiumService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Membuat sesi checkout pembayaran premium", description = "Membuat sesi pembayaran Stripe Checkout riil untuk user agar dapat diaktifkan ke status premium.")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(@Valid @RequestBody CheckoutRequest request) {
        CheckoutSessionResponse response = premiumService.createCheckoutSession(request.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    @Operation(summary = "Halaman sukses pembayaran Stripe", description = "Verifikasi transaksi Stripe dan mengaktifkan status premium user.")
    public String stripeSuccessPage(@RequestParam("session_id") String sessionId, @RequestParam("userId") Long userId) {
        return premiumService.getSuccessPage(sessionId, userId);
    }

    @GetMapping(value = "/cancel", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    @Operation(summary = "Halaman batal pembayaran Stripe", description = "Menampilkan halaman pembatalan transaksi.")
    public String stripeCancelPage(@RequestParam("userId") Long userId) {
        return premiumService.getCancelPage(userId);
    }

    @PostMapping("/payment-intent")
    @Operation(summary = "Membuat Stripe PaymentIntent", description = "Membuat PaymentIntent riil untuk diolah di sisi mobile SDK (PaymentSheet).")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody PaymentIntentRequest request) {
        PaymentIntentResponse response = premiumService.createPaymentIntent(request.userId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-payment")
    @Operation(summary = "Mengonfirmasi status pembayaran Stripe", description = "Memeriksa status akhir PaymentIntent secara aman di server untuk mengaktifkan premium.")
    public ResponseEntity<UserResponse> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        UserResponse response = premiumService.confirmPayment(request.userId(), request.paymentIntentId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook Stripe untuk pembayaran sukses", description = "Webhook Stripe asli untuk memproses notifikasi transaksi sukses secara asinkron.")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        
        log.info("Menerima webhook Stripe. Panjang payload: {}", payload.length());
        
        try {
            Event event;
            if (webhookSecret != null && !webhookSecret.isBlank() && sigHeader != null) {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } else {
                // Fallback untuk local testing tanpa signature verification (misal jika secret tidak diset)
                log.warn("Menerima webhook tanpa secret verifikasi. Menggunakan parser Gson bawaan.");
                event = com.stripe.model.Event.GSON.fromJson(payload, Event.class);
            }

            log.info("Memproses event Stripe: ID={}, Tipe={}", event.getId(), event.getType());
            
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null && "paid".equals(session.getPaymentStatus())) {
                    String userIdStr = session.getMetadata().get("userId");
                    if (userIdStr != null) {
                        Long userId = Long.valueOf(userIdStr);
                        premiumService.handleWebhookPaymentSuccess(userId);
                        log.info("Status premium berhasil diaktifkan melalui webhook untuk user ID: {}", userId);
                    }
                }
            } else if ("payment_intent.succeeded".equals(event.getType())) {
                com.stripe.model.PaymentIntent pi = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) {
                    String userIdStr = pi.getMetadata().get("userId");
                    if (userIdStr != null) {
                        Long userId = Long.valueOf(userIdStr);
                        premiumService.handleWebhookPaymentSuccess(userId);
                        log.info("Status premium berhasil diaktifkan melalui webhook PaymentIntent untuk user ID: {}", userId);
                    }
                }
            }
            
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Gagal memproses webhook Stripe", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: " + e.getMessage());
        }
    }

    // Mock endpoints dipertahankan untuk backward-compatibility
    @GetMapping(value = "/mock-checkout", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    @Operation(summary = "Merender halaman mock checkout", description = "Halaman HTML simulasi antarmuka Stripe Checkout untuk memproses pembayaran palsu.")
    public String mockCheckoutPage(@RequestParam Long userId) {
        return premiumService.getMockCheckoutPage(userId);
    }

    @GetMapping(value = "/mock-success", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    @Operation(summary = "Halaman sukses pembayaran (mock)", description = "Merender halaman HTML transaksi sukses dan memicu proses webhook internal untuk mengaktifkan status premium user.")
    public String mockSuccessPage(@RequestParam Long userId) {
        return premiumService.getMockSuccessPage(userId);
    }
}
