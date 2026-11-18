package com.example.library.web;

import com.example.library.exception.BusinessException;
import org.springframework.validation.BindingResult;

/** Gắn lỗi nghiệp vụ vào BindingResult để hiển thị đúng dưới từng trường. */
public final class FormErrors {

    private FormErrors() {
    }

    public static void bind(BindingResult result, BusinessException ex) {
        if (ex.hasField()) {
            result.rejectValue(ex.getField(), "business", ex.getMessage());
        } else {
            result.reject("business", ex.getMessage());
        }
    }
}
