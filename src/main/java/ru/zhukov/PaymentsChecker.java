package ru.zhukov;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/*
Вы — backend-разработчик в финтех компании.
Компания предоставляет платежные услуги и должна контролировать лимиты пользователей.
Product owner просит создать систему проверки лимитов перед проведением платежей.

## Определения

Платеж:
- id пользователя
- сумма (в рублях с копейками)
- время платежа

Лимиты пользователя: может быть null или exception может и не быть
- суточный лимит по сумме (за 24 часа) - sliding windows
- максимальный размер одного платежа -

История платежей: синхронный
- список ранее совершенных платежей пользователя - sum get(from, to)

## Важно
Настройки лимитов пользователей и история платежей предоставляются другими компонентами системы.
Вам необходимо спроектировать контракты для получения этих данных.
Реализацию этих компонентов делать не нужно.

## Задача
Написать систему проверки лимитов, которая:
- на вход получает платеж
- проверяет лимиты
- возвращает результат проверки: можно ли провести платеж // достаточно одного
- если нельзя, то указывает причину (какой именно лимит будет превышен) // без подробностей

Проведение платежа не входит в вашу задачу - другая команда займется обработкой платежей после проверки.
Ваша задача - только проверка возможности проведения платежа.

## Ограничения
В рамках данной задачи считаем, что все платежи одного пользователя происходят строго последовательно. Во время проверки лимита не может быть проведен платеж того же пользователя.
*/
class PaymentsChecker {
    private final PaymentsHistoryService paymentsHistoryService;
    private final UserLimitsService userLimitsService;

    public PaymentsChecker(
            PaymentsHistoryService paymentsHistoryService,
            UserLimitsService userLimitsService
    ) {
        this.paymentsHistoryService = paymentsHistoryService;
        this.userLimitsService = userLimitsService;
    }

    public PaymentResult checkPayment(Payment payment) {
        BigDecimal maxAccountPaymentLimit;
        try {
            Optional<BigDecimal> maxAccountPaymentLimitOptional = userLimitsService.getOneMaxAmountLimit(payment.accountId());
            maxAccountPaymentLimit = maxAccountPaymentLimitOptional.orElse(BigDecimal.ZERO);
        } catch (Exception e) {
            return new PaymentResult(false, PaymentResultDescription.EXCEPTION);
        }

        if (payment.amount().compareTo(maxAccountPaymentLimit) > 0) {
            return new PaymentResult(false, PaymentResultDescription.MAX_PAYMENT_LIMIT_EXCEED);
        }

        BigDecimal accountSpending;
        try {
            Optional<BigDecimal> accountSpendingOptional = paymentsHistoryService.getAccountSpending(payment.accountId(), payment.createdAt().minus(Duration.ofHours(24)), payment.createdAt());
            accountSpending = accountSpendingOptional.orElse(BigDecimal.ZERO);
        } catch (Exception e) {
            return new PaymentResult(false, PaymentResultDescription.EXCEPTION);
        }

        BigDecimal amountAccountDailyLimit;
        try {
            Optional<BigDecimal> amountAccountDailyLimitOptional = userLimitsService.getAmountByDailyLimit(payment.accountId());
            amountAccountDailyLimit = amountAccountDailyLimitOptional.orElse(BigDecimal.ZERO);
        } catch (Exception e) {
            return new PaymentResult(false, PaymentResultDescription.EXCEPTION);
        }

        if (payment.amount().add(accountSpending).compareTo(amountAccountDailyLimit) > 0) {
            return new PaymentResult(false, PaymentResultDescription.DAILY_LIMIT_EXCEED);
        }

        return new PaymentResult(true, PaymentResultDescription.ALLOW);
    }
}