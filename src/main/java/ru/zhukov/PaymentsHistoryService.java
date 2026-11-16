package ru.zhukov;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface PaymentsHistoryService {

    Optional<BigDecimal> getAccountSpending(String accountId, Instant from, Instant to);
}
