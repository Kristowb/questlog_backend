package com.questlog.backend.controller;

import com.questlog.backend.dto.CheckoutRequest;
import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.service.PremiumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/premium")
@RequiredArgsConstructor
@Tag(name = "Premium API", description = "Manajemen fitur akun premium, integrasi mock Stripe Checkout, dan Webhook")
public class PremiumController {

    private final PremiumService premiumService;

    @PostMapping("/checkout")
    @Operation(summary = "Membuat sesi checkout pembayaran premium", description = "Membuat sesi pembayaran palsu (mock checkout url) untuk user agar dapat diaktifkan ke status premium.")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(@Valid @RequestBody CheckoutRequest request) {
        CheckoutSessionResponse response = premiumService.createCheckoutSession(request.userId());
        return ResponseEntity.ok(response);
    }

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

    @PostMapping("/webhook")
    @Operation(summary = "Webhook Stripe untuk pembayaran sukses", description = "Webhook Stripe asli (dalam mode produksi) untuk memproses notifikasi transaksi sukses.")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        return ResponseEntity.ok("Webhook received");
    }
}
