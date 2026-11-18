package com.example.library.exception;

/** Không tìm thấy bản ghi -> hiển thị trang 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super("Không tìm thấy " + resourceName + " với id = " + id);
    }
}
