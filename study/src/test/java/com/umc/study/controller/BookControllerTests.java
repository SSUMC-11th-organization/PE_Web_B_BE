package com.umc.study.controller;

import com.umc.study.dto.BookResponse;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryRepository;
import com.umc.study.service.BookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookControllerTests {

    private BookService service;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        service = mock(BookService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = mvcFor(service);
    }

    private MockMvc mvcFor(BookService bookService) {
        return MockMvcBuilders.standaloneSetup(new BookController(bookService))
                .setControllerAdvice(new BookExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void getBooksReturnsResponseDto() throws Exception {
        when(service.getBooks()).thenReturn(List.of(
                new BookResponse(2L, "스프링", null, "개발", true)));

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("개발"))
                .andExpect(jsonPath("$[0].isAvailable").value(true));
    }

    @Test
    void createBookReturns201AndSavedResponse() throws Exception {
        when(service.createBook(any())).thenReturn(
                new BookResponse(3L, "스프링", null, "개발", true));

        mvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId": 1, "title": "스프링"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(3))
                .andExpect(jsonPath("$.categoryName").value("개발"));
        verify(service).createBook(any());
    }

    @Test
    void blankTitleIsRejectedBeforeServiceCall() throws Exception {
        mvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
        verifyNoInteractions(service);
    }

    @Test
    void missingCategoryIdIsRejectedBeforeServiceCall() throws Exception {
        mvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"스프링\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.categoryId").exists());
        verifyNoInteractions(service);
    }

    @Test
    void titleOver100CharactersIsRejectedBeforeServiceCall() throws Exception {
        mvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"title\":\"" + "a".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
        verifyNoInteractions(service);
    }

    @Test
    void nonexistentCategoryReturns404WithoutSavingBook() throws Exception {
        BookRepository books = mock(BookRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        when(categories.findById(999L)).thenReturn(Optional.empty());
        MockMvc realServiceMvc = mvcFor(new BookService(books, categories));

        realServiceMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":999,\"title\":\"스프링\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("존재하지 않는 카테고리입니다."));
        verifyNoInteractions(books);
    }
}
