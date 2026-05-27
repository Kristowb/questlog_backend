package com.questlog.backend.service.impl;

import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.dto.PaymentIntentResponse;
import com.questlog.backend.service.UserService;
import com.stripe.StripeClient;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PremiumServiceImplTest {

    @Mock
    private UserService userService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StripeClient stripeClient;

    private PremiumServiceImpl premiumService;

    @BeforeEach
    void setUp() {
        premiumService = new PremiumServiceImpl(
                userService,
                stripeClient,
                "http://localhost:8080/api/v1/premium/success?session_id={CHECKOUT_SESSION_ID}&userId={USER_ID}",
                "http://localhost:8080/api/v1/premium/cancel?userId={USER_ID}",
                "pk_test_mockpublishablekey"
        );
    }

    @Test
    void createCheckoutSession_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        Session mockStripeSession = mock(Session.class);
        when(mockStripeSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_mock");
        
        when(stripeClient.v1().checkout().sessions().create(any(com.stripe.param.checkout.SessionCreateParams.class)))
                .thenReturn(mockStripeSession);

        // Act
        CheckoutSessionResponse response = premiumService.createCheckoutSession(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/pay/cs_test_mock");
        assertThat(response.status()).isEqualTo("SUCCESS");
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getSuccessPage_Paid_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        String sessionId = "cs_test_mock";

        Session mockStripeSession = mock(Session.class);
        when(mockStripeSession.getPaymentStatus()).thenReturn("paid");
        
        when(stripeClient.v1().checkout().sessions().retrieve(sessionId))
                .thenReturn(mockStripeSession);

        // Act
        String htmlResponse = premiumService.getSuccessPage(sessionId, userId);

        // Assert
        assertThat(htmlResponse).contains("Transaksi Berhasil!");
        verify(userService, times(1)).setPremium(userId, true);
    }

    @Test
    void getSuccessPage_Unpaid_Failure() throws Exception {
        // Arrange
        Long userId = 1L;
        String sessionId = "cs_test_mock";

        Session mockStripeSession = mock(Session.class);
        when(mockStripeSession.getPaymentStatus()).thenReturn("unpaid");
        
        when(stripeClient.v1().checkout().sessions().retrieve(sessionId))
                .thenReturn(mockStripeSession);

        // Act
        String htmlResponse = premiumService.getSuccessPage(sessionId, userId);

        // Assert
        assertThat(htmlResponse).contains("Transaksi Gagal");
        verify(userService, never()).setPremium(anyLong(), anyBoolean());
    }

    @Test
    void getCancelPage_Success() {
        // Act
        String htmlResponse = premiumService.getCancelPage(1L);

        // Assert
        assertThat(htmlResponse).contains("Pembayaran dibatalkan");
    }

    @Test
    void createPaymentIntent_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_mock_secret_123");
        when(mockPaymentIntent.getId()).thenReturn("pi_mock");
        
        when(stripeClient.v1().paymentIntents().create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                .thenReturn(mockPaymentIntent);

        // Act
        PaymentIntentResponse response = premiumService.createPaymentIntent(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.paymentIntentClientSecret()).isEqualTo("pi_mock_secret_123");
        assertThat(response.publishableKey()).isEqualTo("pk_test_mockpublishablekey");
        assertThat(response.status()).isEqualTo("SUCCESS");
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void confirmPayment_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        String paymentIntentId = "pi_mock";
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
        
        when(stripeClient.v1().paymentIntents().retrieve(paymentIntentId))
                .thenReturn(mockPaymentIntent);

        // Act
        premiumService.confirmPayment(userId, paymentIntentId);

        // Assert
        verify(userService, times(1)).setPremium(userId, true);
    }

    @Test
    void confirmPayment_Failure_Unpaid() throws Exception {
        // Arrange
        Long userId = 1L;
        String paymentIntentId = "pi_mock";
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getStatus()).thenReturn("requires_payment_method");
        
        when(stripeClient.v1().paymentIntents().retrieve(paymentIntentId))
                .thenReturn(mockPaymentIntent);

        // Act & Assert
        assertThatThrownBy(() -> premiumService.confirmPayment(userId, paymentIntentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status transaksi Stripe belum berhasil: requires_payment_method");
        
        verify(userService, never()).setPremium(anyLong(), anyBoolean());
    }
}
