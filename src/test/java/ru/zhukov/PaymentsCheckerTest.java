package ru.zhukov;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsCheckerTest {

    @Mock
    private PaymentsHistoryService paymentsHistoryService;
    @Mock
    private UserLimitsService userLimitsService;

    @InjectMocks
    private PaymentsChecker paymentsChecker;


    @Test
    void successCheckPaymentTest() {
        String accountId = "account1";
        Instant now = LocalDateTime.of(2026, 1, 1, 12, 1, 1).toInstant(ZoneOffset.UTC);
        Payment payment = new Payment(accountId, now, BigDecimal.valueOf(100));

        when(paymentsHistoryService.getAccountSpending(eq(accountId), any(), any())).thenReturn(Optional.of(BigDecimal.valueOf(50)));
        when(userLimitsService.getAmountByDailyLimit(accountId)).thenReturn(Optional.of(BigDecimal.valueOf(200)));
        when(userLimitsService.getOneMaxAmountLimit(accountId)).thenReturn(Optional.of(BigDecimal.valueOf(150)));

        PaymentResult result = paymentsChecker.checkPayment(payment);

        assertTrue(result.canPay());
        assertEquals(PaymentResultDescription.ALLOW, result.description());
    }


    @Test
    void maxPaymentLimitOverflowTest() {
        String accountId = "account1";
        Instant now = LocalDateTime.of(2026, 1, 1, 12, 1, 1).toInstant(ZoneOffset.UTC);
        Payment payment = new Payment(accountId, now, BigDecimal.valueOf(100));

        when(userLimitsService.getOneMaxAmountLimit(accountId)).thenReturn(Optional.of(BigDecimal.valueOf(50)));

        PaymentResult result = paymentsChecker.checkPayment(payment);

        assertFalse(result.canPay());
        assertEquals(PaymentResultDescription.MAX_PAYMENT_LIMIT_EXCEED, result.description());
    }

    @Test
    void dailyLimitOverflowTest() {
        String accountId = "account1";
        Instant now = LocalDateTime.of(2026, 1, 1, 12, 1, 1).toInstant(ZoneOffset.UTC);
        Payment payment = new Payment(accountId, now, BigDecimal.valueOf(100));

        when(userLimitsService.getOneMaxAmountLimit(accountId)).thenReturn(Optional.of(BigDecimal.valueOf(100)));
        when(paymentsHistoryService.getAccountSpending(eq(accountId), any(), any())).thenReturn(Optional.of(BigDecimal.valueOf(50)));
        when(userLimitsService.getAmountByDailyLimit(accountId)).thenReturn(Optional.of(BigDecimal.valueOf(100)));

        PaymentResult result = paymentsChecker.checkPayment(payment);

        assertFalse(result.canPay());
        assertEquals(PaymentResultDescription.DAILY_LIMIT_EXCEED, result.description());
    }



}