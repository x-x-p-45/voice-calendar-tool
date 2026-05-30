package com.calendar.service.impl;

import com.calendar.entity.Schedule;
import com.calendar.mapper.ScheduleMapper;
import com.calendar.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语音日程业务实现类
 * <p>
 * 核心能力：将语音识别文本自动解析为结构化日程数据
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 语音文本解析核心逻辑
     * <p>
     * 示例：
     * <pre>
     * "明天下午3点开会"      → 自动提取时间 + 事件标题
     * "2025-12-25 14:00 项目汇报" → 明确日期格式识别
     * "今天5点去超市"        → 今天 + 时间
     * "后天上午10点交报告"   → 后天 + 时段 + 时间
     * </pre>
     */
    @Override
    public Schedule analyzeVoice(String voiceText) {
        Schedule schedule = new Schedule();
        schedule.setIsRemind(1);

        Date scheduleTime = extractTime(voiceText);
        schedule.setScheduleTime(scheduleTime);

        // 提取事件标题（去除时间描述后的文本）
        String title = extractTitle(voiceText);
        schedule.setEventTitle(title.isEmpty() ? "语音创建日程" : title);
        schedule.setEventContent("语音解析生成：" + voiceText);

        return schedule;
    }

    /**
     * 从文本中提取日程时间
     * <p>
     * 支持格式：
     * 1. "yyyy-MM-dd HH:mm" 明确时间
     * 2. "今天/明天/后天 + HH:MM/点"
     * 3. "上午/下午/晚上 + HH:MM/点"
     */
    private Date extractTime(String voiceText) {
        Calendar cal = Calendar.getInstance();

        // ── 格式1：明确日期 "2025-12-25 14:00" ──
        Pattern explicitPattern = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2})");
        Matcher explicitMatcher = explicitPattern.matcher(voiceText);
        if (explicitMatcher.find()) {
            try {
                return sdf.parse(explicitMatcher.group(1));
            } catch (ParseException ignored) {
            }
        }

        // ── 确定基准日期（今天/明天/后天） ──
        int dayOffset = 0;
        if (voiceText.contains("后天")) {
            dayOffset = 2;
        } else if (voiceText.contains("明天")) {
            dayOffset = 1;
        }
        cal.add(Calendar.DAY_OF_YEAR, dayOffset);

        // ── 格式2：提取时段 + 时间点 ──
        // 匹配 "上午/下午/晚上 + HH:MM" 或 "HH点"
        Pattern timePattern = Pattern.compile(
                "(上午|下午|晚上|中午)?\\s*(\\d{1,2})[点:：](\\d{0,2})?");
        Matcher timeMatcher = timePattern.matcher(voiceText);

        int hour = 9;  // 默认上午9点
        int minute = 0;

        if (timeMatcher.find()) {
            String period = timeMatcher.group(1);   // 上午/下午/晚上
            hour = Integer.parseInt(timeMatcher.group(2));
            String minStr = timeMatcher.group(3);
            minute = (minStr != null && !minStr.isEmpty()) ? Integer.parseInt(minStr) : 0;

            // 根据时段调整小时
            if (period != null) {
                if (period.contains("下午") || period.contains("晚上")) {
                    if (hour != 12) {
                        hour += 12;
                    }
                } else if (period.contains("中午") && hour < 12) {
                    hour += 12;
                }
            }
        } else {
            // ── 格式3：仅匹配到 "明天/今天/后天" 无具体时间，默认 9:00 ──
            Pattern dayOnly = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})");
            Matcher dayOnlyMatcher = dayOnly.matcher(voiceText);
            if (dayOnlyMatcher.find()) {
                try {
                    cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(dayOnlyMatcher.group(1)));
                    cal.set(Calendar.HOUR_OF_DAY, 9);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    return cal.getTime();
                } catch (ParseException ignored) {
                }
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * 从语音文本中提取日程标题（去除时间相关描述）
     */
    private String extractTitle(String voiceText) {
        String title = voiceText
                // 去除明确日期格式
                .replaceAll("\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}", "")
                // 去除独立日期
                .replaceAll("\\d{4}-\\d{1,2}-\\d{1,2}", "")
                // 去除相对日期词
                .replaceAll("[今天明后]天", "")
                // 去除时段+时间
                .replaceAll("(上午|下午|晚上|中午)?\\s*\\d{1,2}[点:：]\\d{0,2}分?", "")
                // 清理多余空白
                .replaceAll("\\s+", " ")
                .trim();

        return title;
    }

    @Override
    public int addSchedule(Schedule schedule) {
        return scheduleMapper.insertSchedule(schedule);
    }

    @Override
    public List<Schedule> list() {
        return scheduleMapper.selectAll();
    }

    @Override
    public List<Schedule> listByDateRange(Date start, Date end) {
        return scheduleMapper.selectByDateRange(start, end);
    }

    @Override
    public int deleteSchedule(Integer id) {
        return scheduleMapper.deleteById(id);
    }

    @Override
    public int updateRemindStatus(Integer id, Integer isRemind) {
        return scheduleMapper.updateRemindStatus(id, isRemind);
    }
}
