package com.bachratus.expenses_monitor.expense;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderByDateDesc();

    List<Expense> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
