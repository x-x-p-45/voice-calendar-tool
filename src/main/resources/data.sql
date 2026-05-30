MERGE INTO schedule (id, event_title, event_content, schedule_time, is_remind) VALUES
(1, '吃早餐', '语音解析生成：早上八点提醒我吃饭', DATEADD('DAY', 0, CURRENT_DATE) || ' 08:00:00', 1),
(2, '部门会议', '语音解析生成：下午三点半开会', DATEADD('DAY', 0, CURRENT_DATE) || ' 15:30:00', 1),
(3, '年终总结', '语音解析生成：12月25号下午3点开会', '2025-12-25 15:00:00', 1),
(4, '项目周会', '语音解析生成：明天下午3点项目周会', DATEADD('DAY', 1, CURRENT_DATE) || ' 15:00:00', 1),
(5, '提交作业', '语音解析生成：后天上午10点提交作业', DATEADD('DAY', 2, CURRENT_DATE) || ' 10:00:00', 1),
(6, '健身打卡', '语音解析生成：今晚8点健身', DATEADD('DAY', 0, CURRENT_DATE) || ' 20:00:00', 0),
(7, '周报提交', '语音解析生成：下周五汇报', DATEADD('DAY', 7, CURRENT_DATE) || ' 10:00:00', 1);
