package ru.zhukov;

import java.math.BigDecimal;
import java.time.Instant;

public record Payment(String accountId, Instant createdAt, BigDecimal amount) {

}
