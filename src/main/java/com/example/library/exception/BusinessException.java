package com.example.library.exception;

/**
 * Vi phạm quy tắc nghiệp vụ (hết sách, mượn trùng, xoá khi đang được tham chiếu...).
 * Nếu {@link #getField()} khác null, lỗi được gắn vào đúng trường trên form.
 */
public class BusinessException extends RuntimeException {

    private final String field;

    public BusinessException(String message) {
        this(null, message);
    }

    public BusinessException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public boolean hasField() {
        return field != null;
    }
}
