package ru.zhukov;

public record PaymentResult(boolean canPay, PaymentResultDescription description) {
}
