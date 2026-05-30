package com.calendar.service;

import com.calendar.entity.Schedule;

import java.util.Date;
import java.util.List;

/**
 * 语音日程业务接口
 */
public interface ScheduleService {

    /**
     * 解析语音文本生成日程
     * <p>
     * 支持多种中文时间表达：
     * <ul>
     *   <li>明确日期：2025-12-25 14:00</li>
     *   <li>相对日期：明天/今天/后天 + 时间</li>
     *   <li>时段表达：上午/下午/晚上 + 时间点</li>
     * </ul>
     *
     * @param voiceText 语音转文字后的文本
     * @return 解析后的日程实体
     */
    Schedule analyzeVoice(String voiceText);

    /**
     * 新增日程
     *
     * @param schedule 日程实体
     * @return 影响行数
     */
    int addSchedule(Schedule schedule);

    /**
     * 查询所有日程
     *
     * @return 日程列表
     */
    List<Schedule> list();

    /**
     * 按日期范围查询
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 日程列表
     */
    List<Schedule> listByDateRange(Date start, Date end);

    /**
     * 删除日程
     *
     * @param id 日程ID
     * @return 影响行数
     */
    int deleteSchedule(Integer id);

    /**
     * 更新日程提醒状态
     *
     * @param id       日程ID
     * @param isRemind 提醒状态
     * @return 影响行数
     */
    int updateRemindStatus(Integer id, Integer isRemind);
}
