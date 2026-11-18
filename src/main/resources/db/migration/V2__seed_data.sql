-- =========================================================
-- V2: Dữ liệu mẫu để demo
-- =========================================================

INSERT INTO category (name) VALUES
    ('Công nghệ thông tin'),
    ('Văn học'),
    ('Kinh tế'),
    ('Khoa học'),
    ('Kỹ năng sống');

INSERT INTO book (code, title, author, publish_year, quantity, category_id) VALUES
    ('IT001', 'Clean Code',                         'Robert C. Martin',   2008, 5, 1),
    ('IT002', 'Effective Java',                     'Joshua Bloch',       2018, 3, 1),
    ('IT003', 'Spring in Action',                   'Craig Walls',        2022, 2, 1),
    ('IT004', 'Design Patterns',                    'Erich Gamma',        1994, 0, 1),
    ('IT005', 'Refactoring',                        'Martin Fowler',      2018, 4, 1),
    ('VH001', 'Số đỏ',                              'Vũ Trọng Phụng',     1936, 6, 2),
    ('VH002', 'Dế Mèn phiêu lưu ký',                'Tô Hoài',            1941, 8, 2),
    ('VH003', 'Tắt đèn',                            'Ngô Tất Tố',         1939, 2, 2),
    ('VH004', 'Nỗi buồn chiến tranh',               'Bảo Ninh',           1990, 3, 2),
    ('KT001', 'Kinh tế học vĩ mô',                  'N. Gregory Mankiw',  2019, 4, 3),
    ('KT002', 'Cha giàu cha nghèo',                 'Robert Kiyosaki',    1997, 5, 3),
    ('KH001', 'Lược sử thời gian',                  'Stephen Hawking',    1988, 3, 4),
    ('KH002', 'Vũ trụ',                             'Carl Sagan',         1980, 1, 4),
    ('KN001', 'Đắc nhân tâm',                       'Dale Carnegie',      1936, 7, 5),
    ('KN002', 'Thói quen nguyên tử',                'James Clear',        2018, 4, 5);

-- Một lượt đang mượn, một lượt quá hạn, một lượt đã trả
INSERT INTO borrow (book_id, borrower_name, email, borrow_date, due_date, return_date) VALUES
    (1, 'Nguyễn Văn An',  'an.nguyen@example.com', CURDATE() - INTERVAL 3 DAY,  CURDATE() + INTERVAL 11 DAY, NULL),
    (2, 'Trần Thị Bình',  'binh.tran@example.com', CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 6 DAY,  NULL),
    (6, 'Lê Văn Cường',   'cuong.le@example.com',  CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 16 DAY, CURDATE() - INTERVAL 18 DAY);
