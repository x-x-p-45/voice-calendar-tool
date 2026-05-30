package com.calendar.service.impl;

import com.calendar.entity.Schedule;
import com.calendar.mapper.ScheduleMapper;
import com.calendar.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    // 星期映射
    private static final Map<String, Integer> WEEKDAY_MAP = new LinkedHashMap<>();
    static {
        WEEKDAY_MAP.put("周一", 1); WEEKDAY_MAP.put("星期一", 1);
        WEEKDAY_MAP.put("周二", 2); WEEKDAY_MAP.put("星期二", 2);
        WEEKDAY_MAP.put("周三", 3); WEEKDAY_MAP.put("星期三", 3);
        WEEKDAY_MAP.put("周四", 4); WEEKDAY_MAP.put("星期四", 4);
        WEEKDAY_MAP.put("周五", 5); WEEKDAY_MAP.put("星期五", 5);
        WEEKDAY_MAP.put("周六", 6); WEEKDAY_MAP.put("星期六", 6);
        WEEKDAY_MAP.put("周日", 7); WEEKDAY_MAP.put("星期天", 7); WEEKDAY_MAP.put("周天", 7);
    }

    /**
     * 从文本中提取日程时间
     * <p>
     * 支持格式（按优先级）：
     * <ol>
     *   <li>"2025-12-25 14:00" / "2025-12-25 14:00:00" — 完整日期时间</li>
     *   <li>"12月25号/日 下午3点" — 指定月日+时段+时间</li>
     *   <li>"下个月5号 上午10点" — 下月指定日</li>
     *   <li>"下周X 下午3点" — 下周某天</li>
     *   <li>"周X 下午3点" — 本周某天</li>
     *   <li>"25号 下午3点" — 本月某天</li>
     *   <li>"今天/明天/后天/大后天 下午3点" — 相对天数</li>
     *   <li>纯时间 "下午3点" — 今天+时间</li>
     * </ol>
     */
    private Date extractTime(String voiceText) {
        // 统一将中文数字转为阿拉伯数字（影响日期+时间解析）
        voiceText = replaceChineseDigits(voiceText);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // ── 1. 完整日期时间 "2025-12-25 14:00" ──
        Matcher m1 = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}(?::\\d{2})?)").matcher(voiceText);
        if (m1.find()) {
            try { return sdf.parse(m1.group(1).substring(0, 16)); } catch (ParseException ignored) {}
        }

        // ── 2. 解析时间点（小时+分钟） ──
        int[] timeResult = parseTime(voiceText);
        int hour = timeResult[0];
        int minute = timeResult[1];

        // ── 3. 解析日期 ──

        // 3a. 指定月日 "12月25号" "12月25日"
        Matcher mmdd = Pattern.compile("(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*[号日]").matcher(voiceText);
        if (mmdd.find()) {
            int month = Integer.parseInt(mmdd.group(1));
            int day = Integer.parseInt(mmdd.group(2));
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DAY_OF_MONTH, day);
            // 如果该日期已过，推到明年
            if (cal.before(Calendar.getInstance())) {
                cal.add(Calendar.YEAR, 1);
            }
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime();
        }

        // 3b. "下个月X号"
        Matcher nextMonth = Pattern.compile("下个?月\\s*(\\d{1,2})\\s*[号日]?").matcher(voiceText);
        if (nextMonth.find()) {
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            if (nextMonth.group(1) != null) {
                String dayStr = nextMonth.group(1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayStr));
            }
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime();
        }

        // 3c. "下周X" → 下周一/下周二...
        Matcher nextWeek = Pattern.compile("下(?:个)?(周[一二三四五六日天]|星期[一二三四五六日天])").matcher(voiceText);
        if (nextWeek.find()) {
            int targetDow = WEEKDAY_MAP.getOrDefault(nextWeek.group(1), 1);
            int todayDow = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday...7=Saturday
            // 转换到中国星期（1=周一...7=周日）
            int chinaDow = todayDow == 1 ? 7 : todayDow - 1;
            int daysUntil = targetDow - chinaDow;
            if (daysUntil <= 0) daysUntil += 7;
            daysUntil += 7; // 下周
            cal.add(Calendar.DAY_OF_YEAR, daysUntil);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime();
        }

        // 3d. "周X" → 本周X
        Matcher thisWeek = Pattern.compile("(周[一二三四五六日天]|星期[一二三四五六日天])").matcher(voiceText);
        if (thisWeek.find()) {
            int targetDow = WEEKDAY_MAP.getOrDefault(thisWeek.group(1), 1);
            int todayDow = cal.get(Calendar.DAY_OF_WEEK);
            int chinaDow = todayDow == 1 ? 7 : todayDow - 1;
            int daysUntil = targetDow - chinaDow;
            if (daysUntil < 0) daysUntil += 7;
            // daysUntil==0 表示今天就是目标星期，仍然用今天
            cal.add(Calendar.DAY_OF_YEAR, daysUntil);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime();
        }

        // 3e. "X号" → 本月X号
        Matcher dayOnly = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*[号日](?!\\d)").matcher(voiceText);
        if (dayOnly.find()) {
            int day = Integer.parseInt(dayOnly.group(1));
            cal.set(Calendar.DAY_OF_MONTH, day);
            if (cal.before(Calendar.getInstance())) {
                cal.add(Calendar.MONTH, 1); // 已过则推下月
            }
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime();
        }

        // 3f. 相对天数 今天/明天/后天/大后天
        int dayOffset = 0;
        if (voiceText.contains("大后天")) dayOffset = 3;
        else if (voiceText.contains("后天")) dayOffset = 2;
        else if (voiceText.contains("明天")) dayOffset = 1;
        cal.add(Calendar.DAY_OF_YEAR, dayOffset);

        // ── 4. 仅日期无时间（如 "2025-12-25"） ──
        Matcher dateOnly = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})").matcher(voiceText);
        if (dateOnly.find()) {
            try {
                cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(dateOnly.group(1)));
            } catch (ParseException ignored) {}
        }

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTime();
    }

    /**
     * 从文本中提取时间（小时+分钟）
     * 支持：上午/下午/晚上/中午/凌晨/早上 + 数字/中文数字 点/时/:
     * 例："早上八点" → [8,0], "下午三点半" → [15,30], "晚上8:30" → [20,30]
     * @return [hour, minute]
     */
    private int[] parseTime(String voiceText) {
        Pattern p = Pattern.compile("(凌晨|早上|上午|中午|下午|晚上)?\\s*(\\d{1,2})\\s*[点:：时]\\s*((\\d{1,2})\\s*分?)?");
        Matcher m = p.matcher(voiceText);
        if (m.find()) {
            String period = m.group(1);
            int h = Integer.parseInt(m.group(2));
            int min = 0;
            if (m.group(4) != null) {
                min = Integer.parseInt(m.group(4));
            }

            if (period != null) {
                if (period.contains("凌晨")) {
                    if (h == 12) h = 0;
                } else if (period.contains("早上")) {
                    // 保持原值
                } else if (period.contains("上午")) {
                    if (h == 12) h = 0;
                } else if (period.contains("中午")) {
                    if (h < 12) h += 12;
                } else if (period.contains("下午")) {
                    if (h != 12) h += 12;
                } else if (period.contains("晚上")) {
                    if (h != 12) h += 12;
                }
            }
            return new int[]{h, min};
        }
        return new int[]{9, 0};
    }

    /** 将中文数字替换为阿拉伯数字（支持 零～三十 以及 半→30分） */
    private String replaceChineseDigits(String text) {
        // 1. 单个汉字替换
        String[][] map = {{"零","0"},{"一","1"},{"二","2"},{"两","2"},{"三","3"},
                          {"四","4"},{"五","5"},{"六","6"},{"七","7"},{"八","8"},{"九","9"}};
        String result = text;
        for (String[] pair : map) result = result.replace(pair[0], pair[1]);
        // 2. 复合：X十Y → 1X/Y  (如 "十二"→"12", "二十五"→"25")
        result = result.replaceAll("([1-9])十(\\d)", "$1$2");
        // 3. 十Y → 1Y (如 "十二"→"12")
        result = result.replaceAll("十(\\d)", "1$1");
        // 4. X十 → X0 (如 "二十"→"20")
        result = result.replaceAll("([1-9])十", "$10");
        // 5. 纯 "十" → "10"
        result = result.replace("十", "10");
        // 6. 半 → 30分
        result = result.replace("半", "30分");
        return result;
    }

    /**
     * 从语音文本中提取日程标题（去除时间相关描述）
     */
    private String extractTitle(String voiceText) {
        String title = voiceText
                .replaceAll("\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}(?::\\d{2})?", "")
                .replaceAll("\\d{4}-\\d{1,2}-\\d{1,2}", "")
                .replaceAll("\\d{1,2}\\s*月\\s*\\d{1,2}\\s*[号日]", "")
                .replaceAll("\\d{1,2}\\s*[号日]", "")
                .replaceAll("下个?月\\s*\\d{0,2}\\s*[号日]?", "")
                .replaceAll("下(?:个)?(?:周[一二三四五六日天]|星期[一二三四五六日天])", "")
                .replaceAll("(?:周[一二三四五六日天]|星期[一二三四五六日天])", "")
                .replaceAll("[今明后大]后天?", "")
                .replaceAll("(凌晨|早上|上午|中午|下午|晚上)?\\s*\\d{1,2}\\s*[点:：时]\\s*\\d{0,2}\\s*分?", "")
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
        // 将 end 设为当天 23:59:59，确保包含结束日期当天的所有日程
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return scheduleMapper.selectByDateRange(start, cal.getTime());
    }

    @Override
    public int deleteSchedule(Integer id) {
        return scheduleMapper.deleteById(id);
    }

    @Override
    public int updateRemindStatus(Integer id, Integer isRemind) {
        return scheduleMapper.updateRemindStatus(id, isRemind);
    }

    @Override
    public int batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return scheduleMapper.batchDelete(ids);
    }

    @Override
    public int batchUpdateRemind(List<Integer> ids, Integer isRemind) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return scheduleMapper.batchUpdateRemind(ids, isRemind);
    }

    @Override
    public List<Schedule> searchByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return scheduleMapper.selectAll();
        }
        return scheduleMapper.searchByTitle(keyword.trim());
    }
}
