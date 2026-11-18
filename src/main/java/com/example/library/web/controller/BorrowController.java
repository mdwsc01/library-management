package com.example.library.web.controller;

import com.example.library.dto.BorrowForm;
import com.example.library.entity.Borrow;
import com.example.library.exception.BusinessException;
import com.example.library.service.BookService;
import com.example.library.service.BorrowService;
import com.example.library.service.BorrowStatus;
import com.example.library.web.FormErrors;
import jakarta.validation.Valid;
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
@RequestMapping("/borrows")
public class BorrowController {

    private static final String FORM_VIEW = "borrows/form";
    private static final int DEFAULT_LOAN_DAYS = 14;

    private final BorrowService borrowService;
    private final BookService bookService;

    public BorrowController(BorrowService borrowService, BookService bookService) {
        this.borrowService = borrowService;
        this.bookService = bookService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        BorrowStatus current = BorrowStatus.fromParam(status);
        model.addAttribute("borrows", borrowService.findByStatus(current, page));
        model.addAttribute("status", current);
        model.addAttribute("statuses", BorrowStatus.values());
        model.addAttribute("today", borrowService.today());
        model.addAttribute("activeCount", borrowService.countActive());
        model.addAttribute("overdueCount", borrowService.countOverdue());
        return "borrows/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long bookId, Model model) {
        BorrowForm form = new BorrowForm();
        form.setBookId(bookId);
        form.setDueDate(borrowService.today().plusDays(DEFAULT_LOAN_DAYS));
        return showForm(form, model);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("borrowForm") BorrowForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(form, model);
        }
        try {
            Borrow borrow = borrowService.borrow(form);
            redirect.addFlashAttribute("successMessage",
                    borrow.getBorrowerName() + " đã mượn '" + borrow.getBook().getTitle()
                            + "', hạn trả " + borrow.getDueDate());
            return "redirect:/borrows";
        } catch (BusinessException ex) {
            FormErrors.bind(result, ex);
            return showForm(form, model);
        }
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Borrow borrow = borrowService.returnBook(id);
            redirect.addFlashAttribute("successMessage",
                    "Đã nhận trả '" + borrow.getBook().getTitle() + "' từ " + borrow.getBorrowerName());
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/borrows";
    }

    private String showForm(BorrowForm form, Model model) {
        model.addAttribute("borrowForm", form);
        model.addAttribute("availableBooks", bookService.findAvailable());
        return FORM_VIEW;
    }
}
