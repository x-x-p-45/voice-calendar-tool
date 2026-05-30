CREATE TABLE IF NOT EXISTS schedule (
    id            INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_title   VARCHAR(100) NOT NULL                  COMMENT '日程标题',
    event_content TEXT                                    COMMENT '日程详情描述',
    schedule_time DATETIME     NOT NULL                  COMMENT '日程执行时间',
    is_remind     TINYINT      DEFAULT 0                 COMMENT '提醒状态：0-不提醒 1-提醒',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'
);
