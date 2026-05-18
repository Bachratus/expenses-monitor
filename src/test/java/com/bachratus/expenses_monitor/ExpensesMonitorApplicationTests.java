package com.bachratus.expenses_monitor;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bachratus.expenses_monitor.category.CategoryRepository;
import com.bachratus.expenses_monitor.expense.ExpenseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExpensesMonitorApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ExpenseRepository expenseRepository;

	@BeforeEach
	void clearDatabase() {
		expenseRepository.deleteAll();
		categoryRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createsAndListsCategories() throws Exception {
		mockMvc.perform(post("/api/categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "  Food  "
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Food"));

		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name").value("Food"));
	}

	@Test
	void rejectsDuplicateCategoryNamesIgnoringCase() throws Exception {
		createCategory("Food");

		mockMvc.perform(post("/api/categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "food"
								}
								"""))
				.andExpect(status().isConflict());
	}

	@Test
	void createsListsAndFiltersExpenses() throws Exception {
		long categoryId = createCategory("Food");

		createExpense("25.50", categoryId, "2026-05-15");
		createExpense("12.30", categoryId, "2026-06-01");

		mockMvc.perform(get("/api/expenses"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].amount").value(12.30))
				.andExpect(jsonPath("$[0].date").value("2026-06-01"))
				.andExpect(jsonPath("$[0].category.name").value("Food"))
				.andExpect(jsonPath("$[1].amount").value(25.50))
				.andExpect(jsonPath("$[1].date").value("2026-05-15"));

		mockMvc.perform(get("/api/expenses/range")
						.param("from", "2026-05-01")
						.param("to", "2026-05-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].amount").value(25.50))
				.andExpect(jsonPath("$[0].date").value("2026-05-15"));
	}

	@Test
	void rejectsExpenseWithInvalidAmount() throws Exception {
		long categoryId = createCategory("Food");

		mockMvc.perform(post("/api/expenses")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 0,
								  "categoryId": %d,
								  "date": "2026-05-15"
								}
								""".formatted(categoryId)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsExpenseWithMissingCategory() throws Exception {
		mockMvc.perform(post("/api/expenses")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 25.50,
								  "categoryId": 999,
								  "date": "2026-05-15"
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidDateRange() throws Exception {
		mockMvc.perform(get("/api/expenses/range")
						.param("from", "2026-06-01")
						.param("to", "2026-05-01"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsDateThatDoesNotExist() throws Exception {
		mockMvc.perform(get("/api/expenses/range")
						.param("from", "2026-01-01")
						.param("to", "2026-06-31"))
				.andExpect(status().isBadRequest());
	}

	private long createCategory(String name) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "%s"
								}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.get("id").asLong();
	}

	private void createExpense(String amount, long categoryId, String date) throws Exception {
		mockMvc.perform(post("/api/expenses")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": %s,
								  "categoryId": %d,
								  "date": "%s"
								}
								""".formatted(amount, categoryId, date)))
				.andExpect(status().isCreated());
	}

}
