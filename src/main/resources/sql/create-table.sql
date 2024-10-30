CREATE TABLE users
(
    id                  BINARY(16)   NOT NULL,        -- UUID로 사용자 고유 식별자
    email               VARCHAR(255) NOT NULL UNIQUE, -- 고유한 이메일 필드
    password_hash       VARCHAR(255),                 -- 비밀번호 해시 값 저장
    name                VARCHAR(255) NOT NULL,        -- 사용자 이름
    profile_picture_url TEXT,                         -- 프로필 사진 URL 저장, 문자열로 변환된 JSON 형식
    role                VARCHAR(50),                  -- ENUM 타입으로 역할 정보 저장
    is_deleted          BIT DEFAULT b'0',             -- 삭제 여부 플래그
    deleted_at          DATETIME     NULL,            -- 삭제된 시간 (NULL 가능)
    created_at          DATETIME,                     -- 생성 시간
    updated_at          DATETIME,                     -- 수정 시간

    PRIMARY KEY (id)                                  -- Primary Key 설정
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE diaries
(
    id         BINARY(16) NOT NULL,
    user_id    BINARY(16) NOT NULL,
    title      VARCHAR(255),
    content    TEXT,
    is_temp    BIT        NOT NULL DEFAULT b'1',
    is_deleted BIT        NOT NULL DEFAULT b'0',
    deleted_at DATETIME            DEFAULT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id) -- users 테이블의 id와 외래키 설정
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE diary_media
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BINARY(16)   NOT NULL,
    diary_id   BINARY(16)   NOT NULL,
    file_url   VARCHAR(255) NOT NULL,
    file_type  VARCHAR(50)  NOT NULL,
    file_name  VARCHAR(50)  NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),    -- users 테이블의 id와 외래키 설정
    FOREIGN KEY (diary_id) REFERENCES diaries (id), -- diaries 테이블의 id와 외래키 설정
    INDEX idx_file_name_prefix (file_name(16))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE emotion_analysis
(
    id               BINARY(16) NOT NULL,
    user_id          BINARY(16) NOT NULL,
    diary_id         BINARY(16) NOT NULL,
    primary_emotion  VARCHAR(50),
    emotion_score    TINYINT,
    analysis_content TEXT,
    date             DATE,
    is_deleted       BIT        NOT NULL DEFAULT b'0',
    deleted_at       DATETIME            DEFAULT NULL,
    created_at       DATETIME,
    updated_at       DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (diary_id) REFERENCES diaries (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;