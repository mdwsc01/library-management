-- =========================================================
-- V1: Tạo bảng cho hệ thống quản lý thư viện
-- =========================================================

CREATE TABLE category (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE book (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    code         VARCHAR(20)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    author       VARCHAR(150) NOT NULL,
    publish_year INT          NOT NULL,
    quantity     INT          NOT NULL DEFAULT 0,  -- số cuốn còn trong kho (có thể cho mượn)
    category_id  BIGINT       NOT NULL,
    CONSTRAINT pk_book PRIMARY KEY (id),
    CONSTRAINT uk_book_code UNIQUE (code),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT ck_book_quantity CHECK (quantity >= 0),
    CONSTRAINT ck_book_publish_year CHECK (publish_year BETWEEN 1000 AND 9999)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Phục vụ tìm kiếm / sắp xếp / lọc
CREATE INDEX idx_book_title    ON book (title);
CREATE INDEX idx_book_author   ON book (author);
CREATE INDEX idx_book_category ON book (category_id);

CREATE TABLE borrow (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    book_id       BIGINT       NOT NULL,
    borrower_name VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,   -- luôn lưu chữ thường
    borrow_date   DATE         NOT NULL,
    due_date      DATE         NOT NULL,
    return_date   DATE         NULL,       -- NULL = chưa trả
    -- Cột sinh tự động: = 1 khi đang mượn, NULL khi đã trả.
    -- MySQL cho phép nhiều NULL trong UNIQUE nên chỉ chặn trùng các lượt CHƯA trả.
    active_flag   TINYINT AS (CASE WHEN return_date IS NULL THEN 1 ELSE NULL END) STORED,
    CONSTRAINT pk_borrow PRIMARY KEY (id),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT ck_borrow_due_date CHECK (due_date >= borrow_date),
    CONSTRAINT ck_borrow_return_date CHECK (return_date IS NULL OR return_date >= borrow_date),
    -- Một email không được mượn trùng một cuốn khi chưa trả (chốt chặn cuối ở tầng DB)
    CONSTRAINT uk_borrow_active UNIQUE (book_id, email, active_flag)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Danh sách đang mượn / quá hạn: lọc theo return_date IS NULL và due_date
CREATE INDEX idx_borrow_return_due ON borrow (return_date, due_date);
