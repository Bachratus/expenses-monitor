package com.bachratus.expenses_monitor.expense;

import com.bachratus.expenses_monitor.category.Category;
import com.bachratus.expenses_monitor.category.CategoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public Expense createExpense(BigDecimal amount, Long categoryId, LocalDate date) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        if (categoryId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category id is required");
        if (date == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Expense expense = new Expense(amount, category, date);
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpenses() {
        return expenseRepository.findAllByOrderByDateDesc();
    }

    public List<Expense> getExpensesByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both from and to dates are required");
        if (from.isAfter(to))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From date cannot be after to date");

        return expenseRepository.findByDateBetweenOrderByDateAsc(from, to);
    }
}
