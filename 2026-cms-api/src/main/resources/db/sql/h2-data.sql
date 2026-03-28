-- TODO data
-- example
-- insert into members (name, last_modified_date) values('bob', now());
-- Member 데이터 삽입 (비밀번호: 1234)
INSERT INTO members (username, password, name, role, created_date, created_by)
VALUES ('admin', '$2a$12$EJtQU85FLQAVpqigluLbIOvRUb1Ope7OJ6tzDcLYi1Q5A6yIFTWR.', '관리자', 'ADMIN', NOW(), 'SYSTEM');

INSERT INTO members (username, password, name, role, created_date, created_by)
VALUES ('user1', '$2a$12$EJtQU85FLQAVpqigluLbIOvRUb1Ope7OJ6tzDcLYi1Q5A6yIFTWR.', '테스트유저1', 'MEMBER', NOW(), 'SYSTEM');

INSERT INTO members (username, password, name, role, created_date, created_by)
VALUES ('user2', '$2a$12$EJtQU85FLQAVpqigluLbIOvRUb1Ope7OJ6tzDcLYi1Q5A6yIFTWR.', '테스트유저2', 'MEMBER', NOW(), 'SYSTEM');

INSERT INTO contents (title, description, view_count, member_id, created_date, created_by)
VALUES ('첫 번째 공지사항', '관리자가 작성한 공지사항 내용입니다.', 10, 1, NOW(), 'admin');

INSERT INTO contents (title, description, view_count, member_id, created_date, created_by)
VALUES ('테스트 게시물 1', '유저1이 작성한 본문입니다.', 0, 2, NOW(), 'user1');

INSERT INTO contents (title, description, view_count, member_id, created_date, created_by)
VALUES ('테스트 게시물 2', '유저2가 작성한 본문입니다.', 5, 3, NOW(), 'user2');

INSERT INTO contents (title, description, view_count, member_id, created_date, created_by)
VALUES ('페이징 테스트용 게시물', '목록 조회를 위한 데이터입니다.', 0, 2, NOW(), 'user1');