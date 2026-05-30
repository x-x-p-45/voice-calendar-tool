-- =============================================
-- 语音日历工具 — 数据库初始化脚本
-- 适用数据库：MySQL 5.7+ / 8.0+
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS voice_calendar
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE voice_calendar;

-- 创建日程表
CREATE TABLE IF NOT EXISTS schedule (
    id            INT           PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_title   VARCHAR(100)  NOT NULL                  COMMENT '日程标题',
    event_content TEXT                                    COMMENT '日程详情描述',
    schedule_time DATETIME      NOT NULL                  COMMENT '日程执行时间',
    is_remind     TINYINT       DEFAULT 0                 COMMENT '提醒状态：0-不提醒 1-提醒',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语音日程表';

-- 插入测试数据
INSERT INTO schedule (event_title, event_content, schedule_time, is_remind) VALUES
('项目周会',       '语音解析生成：明天下午3点项目周会',           '2025-12-26 15:00:00', 1),
('期末复习计划',   '语音解析生成：今天晚上8点期末复习计划',       '2025-12-25 20:00:00', 1),
('提交实训作品',   '语音解析生成：12月30日上午10点提交实训作品', '2025-12-30 10:00:00', 0);
