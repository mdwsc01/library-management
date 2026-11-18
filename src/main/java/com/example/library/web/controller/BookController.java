package com.example.library.web.controller;

import com.example.library.dto.BookForm;
import com.example.library.entity.Book;
import com.example.library.exception.BusinessException;
import com.example.library.service.BookSearchCriteria;
import com.example.library.service.BookService;
import com.example.library.service.CategoryService;
import com.example.library.web.FormErrors;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
public class BookController {

    private static final String FORM_VIEW = "books/form";

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "title") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        BookSearchCriteria criteria = new BookSearchCriteria(keyword, categoryId, page, size, sort, dir);
        Page<Book> books = bookService.search(criteria);

        model.addAttribute("books", books);
        model.addAttribute("criteria", criteria);
        model.addAttribute("categories", categoryService.findAll());
        return "books/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        return showForm(new BookForm(), model);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("bookForm") BookForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(form, model);
        }
        try {
            Book book = bookService.create(form);
            redirect.addFlashAttribute("successMessage", "Đã thêm sách '" + book.getTitle() + "'");
            return "redirect:/books";
        } catch (BusinessException ex) {
            FormErrors.bind(result, ex);
            return showForm(form, model);
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return showForm(BookForm.from(bookService.findById(id)), model);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("bookForm") BookForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        form.setId(id);
        if (result.hasErrors()) {
            return showForm(form, model);
        }
        try {
            Book book = bookService.update(id, form);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật sách '" + book.getTitle() + "'");
            return "redirect:/books";
        } catch (BusinessException ex) {
            FormErrors.bind(result, ex);
            return showForm(form, model);
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            bookService.delete(id);
            redirect.addFlashAttribute("successMessage", "Đã xoá sách");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/books";
    }

    private String showForm(BookForm form, Model model) {
        model.addAttribute("bookForm", form);
        model.addAttribute("categories", categoryService.findAll());
        return FORM_VIEW;
    }
}
