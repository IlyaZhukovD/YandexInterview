package ru.zhukov;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserLimitsService {

    Optional<BigDecimal> getAmountByDailyLimit(String accountId);

    Optional<BigDecimal> getOneMaxAmountLimit(String accountId);
}
