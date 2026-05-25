package com.questlog.backend.service.impl;

import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.service.PremiumService;
import com.questlog.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumServiceImpl implements PremiumService {

    private final UserService userService;

    @Override
    public CheckoutSessionResponse createCheckoutSession(Long userId) {
        log.info("Membuat sesi pembayaran premium (mock) untuk user ID: {}", userId);
        // Memanggil getUserById untuk memvalidasi keberadaan user (akan melempar Exception jika tidak ditemukan)
        userService.getUserById(userId);

        String mockCheckoutUrl = "http://localhost:8080/api/v1/premium/mock-checkout?userId=" + userId;

        log.info("Sesi pembayaran premium (mock) berhasil dibuat untuk user ID: {}", userId);
        return new CheckoutSessionResponse(mockCheckoutUrl, "SUCCESS_MOCK");
    }

    @Override
    @Transactional
    public UserResponse handleWebhookPaymentSuccess(Long userId) {
        log.info("Menerima notifikasi pembayaran sukses untuk user ID: {}. Mengaktifkan fitur premium.", userId);
        return userService.setPremium(userId, true);
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
                    <input type="hidden" name="userId" value="%d">
                    <button type="submit" class="btn-pay">Bayar via Stripe Crypto</button>
                </form>
            </div>
        </body>
        </html>
        """.formatted(userId);
    }

    @Override
    public String getMockSuccessPage(Long userId) {
        log.info("Memproses webhook sukses pembayaran premium untuk user ID: {}", userId);
        handleWebhookPaymentSuccess(userId);
        
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
                <p>Status akun Anda telah berhasil diperbarui menjadi <strong>Premium Adventurer</strong> di Supabase Database.</p>
                <p class="back-info">Anda sekarang dapat kembali ke aplikasi QuestLog.</p>
            </div>
        </body>
        </html>
        """;
    }
}
