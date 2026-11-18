package com.example.library.service;

/** Bộ lọc trên trang danh sách mượn. */
public enum BorrowStatus {
    ACTIVE("Đang mượn"),
    OVERDUE("Quá hạn"),
    RETURNED("Đã trả");

    private final String label;

    BorrowStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Chuyển từ query string, giá trị lạ thì mặc định ACTIVE. */
    public static BorrowStatus fromParam(String value) {
        if (value == null) {
            return ACTIVE;
        }
        for (BorrowStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
