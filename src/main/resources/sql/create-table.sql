CREATE TABLE `users` (
                         `id` binary(16) NOT NULL,
                         `email` varchar(255) NOT NULL,
                         `password_hash` varchar(255) DEFAULT NULL,
                         `name` varchar(255) NOT NULL,
                         `profile_picture_url` varchar(255) DEFAULT NULL,
                         `role` varchar(50) DEFAULT NULL,
                         `is_deleted` bit(1) DEFAULT b'0',
                         `deleted_at` datetime DEFAULT NULL,
                         `created_at` datetime DEFAULT NULL,
                         `updated_at` datetime DEFAULT NULL,
                         `social_provider` varchar(50) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diaries` (
                           `id` binary(16) NOT NULL,
                           `user_id` binary(16) NOT NULL,
                           `content` text,
                           `is_temp` bit(1) NOT NULL DEFAULT b'1',
                           `is_deleted` bit(1) NOT NULL DEFAULT b'0',
                           `deleted_at` datetime DEFAULT NULL,
                           `created_at` datetime DEFAULT NULL,
                           `updated_at` datetime DEFAULT NULL,
                           `date` date DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           KEY `user_id` (`user_id`),
                           CONSTRAINT `diaries_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary_media` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `user_id` binary(16) NOT NULL,
                               `diary_id` binary(16) NOT NULL,
                               `file_url` varchar(255) NOT NULL,
                               `file_type` varchar(255) NOT NULL,
                               `file_name` varchar(255) NOT NULL,
                               `created_at` datetime DEFAULT NULL,
                               `updated_at` datetime DEFAULT NULL,
                               PRIMARY KEY (`id`),
                               KEY `user_id` (`user_id`),
                               KEY `diary_id` (`diary_id`),
                               KEY `idx_file_name_prefix` (`file_name`(16)),
                               CONSTRAINT `diary_media_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                               CONSTRAINT `diary_media_ibfk_2` FOREIGN KEY (`diary_id`) REFERENCES `diaries` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `emotion_analysis` (
                                    `id` binary(16) NOT NULL,
                                    `user_id` binary(16) NOT NULL,
                                    `diary_id` binary(16) NOT NULL,
                                    `primary_emotion` varchar(50) DEFAULT NULL,
                                    `emotion_score` tinyint DEFAULT NULL,
                                    `analysis_content` text,
                                    `date` date DEFAULT NULL,
                                    `is_deleted` bit(1) NOT NULL DEFAULT b'0',
                                    `deleted_at` datetime DEFAULT NULL,
                                    `created_at` datetime DEFAULT NULL,
                                    `updated_at` datetime DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `user_id` (`user_id`),
                                    KEY `diary_id` (`diary_id`),
                                    CONSTRAINT `emotion_analysis_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                                    CONSTRAINT `emotion_analysis_ibfk_2` FOREIGN KEY (`diary_id`) REFERENCES `diaries` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fcm_token` (
                                    `id` binary(16) NOT NULL,
                                    `user_id` binary(16) NOT NULL,
                                    `device_id` VARCHAR(255) NOT NULL,
                                    `fcm_token` TEXT NOT NULL,
                                    `device_type` VARCHAR(50) NOT NULL,
                                    `is_active` BOOLEAN DEFAULT TRUE,
                                    `created_at` datetime DEFAULT NULL,
                                    `updated_at` datetime DEFAULT NULL,
                                    `last_used_at` datetime DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `user_id` (`user_id`),
                                    CONSTRAINT `fcm_token_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;