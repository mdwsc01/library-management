package com.example.library.web.controller;

import com.example.library.dto.CategoryForm;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.service.CategoryService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private static final String FORM_VIEW = "categories/form";

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAllWithBookCount());
        return "categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            Category category = categoryService.create(form);
            redirect.addFlashAttribute("successMessage", "Đã thêm thể loại '" + category.getName() + "'");
            return "redirect:/categories";
        } catch (BusinessException ex) {
            FormErrors.bind(result, ex);
            return FORM_VIEW;
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoryForm", CategoryForm.from(categoryService.findById(id)));
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult result,
                         RedirectAttributes redirect) {
        form.setId(id);
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            Category category = categoryService.update(id, form);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật thể loại '" + category.getName() + "'");
            return "redirect:/categories";
        } catch (BusinessException ex) {
            FormErrors.bind(result, ex);
            return FORM_VIEW;
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            categoryService.delete(id);
            redirect.addFlashAttribute("successMessage", "Đã xoá thể loại");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }
}
