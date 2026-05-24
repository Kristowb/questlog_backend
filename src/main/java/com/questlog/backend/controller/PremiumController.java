package com.questlog.backend.controller;

import com.questlog.backend.dto.CheckoutRequest;
import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.service.PremiumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final PremiumService premiumService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(@Valid @RequestBody CheckoutRequest request) {
        CheckoutSessionResponse response = premiumService.createCheckoutSession(request.userId());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/mock-checkout", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String mockCheckoutPage(@RequestParam Long userId) {
        return premiumService.getMockCheckoutPage(userId);
    }

    @GetMapping(value = "/mock-success", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String mockSuccessPage(@RequestParam Long userId) {
        return premiumService.getMockSuccessPage(userId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        return ResponseEntity.ok("Webhook received");
    }
}
