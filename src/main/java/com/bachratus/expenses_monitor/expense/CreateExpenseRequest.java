package com.bachratus.expenses_monitor.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        BigDecimal amount,
        Long categoryId,
        LocalDate date) {
}
