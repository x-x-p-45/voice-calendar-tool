package com.calendar.controller;

import com.calendar.common.R;
import com.calendar.entity.Schedule;
import com.calendar.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 语音日程接口控制器
 * <p>
 * 提供 RESTful API 供前端/语音客户端调用
 */
@Tag(name = "语音日程管理", description = "语音解析创建、查询、删除、提醒状态管理")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 语音文本解析并创建日程
     */
    @Operation(summary = "语音文本创建日程", description = "接收语音识别后的文本，自动解析时间与事件，创建日程记录")
    @PostMapping("/voiceAdd")
    public R<String> voiceAdd(
            @Parameter(description = "语音转文字后的文本，例如：明天下午3点项目周会") @RequestParam String voiceText) {
        if (voiceText == null || voiceText.trim().isEmpty()) {
            return R.error("语音文本不能为空");
        }
        try {
            Schedule schedule = scheduleService.analyzeVoice(voiceText.trim());
            scheduleService.addSchedule(schedule);
            return R.success("语音日程创建成功");
        } catch (Exception e) {
            return R.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有日程列表
     */
    @Operation(summary = "查询所有日程", description = "返回按时间倒序排列的全部日程列表")
    @GetMapping("/list")
    public R<List<Schedule>> list() {
        return R.success(scheduleService.list());
    }

    /**
     * 按日期范围查询日程
     */
    @Operation(summary = "按日期范围查询", description = "查询指定日期区间内的日程，按时间升序排列")
    @GetMapping("/listByRange")
    public R<List<Schedule>> listByRange(
            @Parameter(description = "开始日期，格式 yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @Parameter(description = "结束日期，格式 yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        if (start == null || end == null) {
            return R.error("请提供开始和结束日期");
        }
        if (start.after(end)) {
            return R.error("开始日期不能晚于结束日期");
        }
        return R.success(scheduleService.listByDateRange(start, end));
    }

    /**
     * 根据 ID 删除日程
     */
    @Operation(summary = "删除日程", description = "根据日程 ID 删除指定的日程记录")
    @DeleteMapping("/delete/{id}")
    public R<String> delete(
            @Parameter(description = "日程 ID") @PathVariable Integer id) {
        if (id == null || id <= 0) {
            return R.error("无效的日程ID");
        }
        int rows = scheduleService.deleteSchedule(id);
        if (rows > 0) {
            return R.success("删除成功");
        }
        return R.error("日程不存在");
    }

    /**
     * 更新日程提醒状态
     */
    @Operation(summary = "更新提醒状态", description = "切换指定日程的提醒开关状态（0=不提醒，1=提醒）")
    @PutMapping("/remind/{id}")
    public R<String> updateRemind(
            @Parameter(description = "日程 ID") @PathVariable Integer id,
            @Parameter(description = "提醒状态：0-不提醒，1-提醒") @RequestParam Integer isRemind) {
        if (isRemind == null || (isRemind != 0 && isRemind != 1)) {
            return R.error("提醒状态只能为 0 或 1");
        }
        int rows = scheduleService.updateRemindStatus(id, isRemind);
        if (rows > 0) {
            return R.success("提醒状态更新成功");
        }
        return R.error("日程不存在");
    }

    /**
     * 批量删除日程
     *
     * @param ids 日程 ID 列表（逗号分隔，如 1,2,3）
     * @return 删除结果
     */
    @DeleteMapping("/batchDelete")
    public R<String> batchDelete(@RequestParam List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return R.error("请提供要删除的日程ID列表");
        }
        int rows = scheduleService.batchDelete(ids);
        return R.success("成功删除 " + rows + " 条日程");
    }

    /**
     * 批量更新提醒状态
     *
     * @param ids      日程 ID 列表（逗号分隔）
     * @param isRemind 提醒状态（0/1）
     * @return 更新结果
     */
    @PutMapping("/batchRemind")
    public R<String> batchUpdateRemind(@RequestParam List<Integer> ids, @RequestParam Integer isRemind) {
        if (ids == null || ids.isEmpty()) {
            return R.error("请提供日程ID列表");
        }
        if (isRemind == null || (isRemind != 0 && isRemind != 1)) {
            return R.error("提醒状态只能为 0 或 1");
        }
        int rows = scheduleService.batchUpdateRemind(ids, isRemind);
        return R.success("成功更新 " + rows + " 条日程提醒状态");
    }

    /**
     * 按关键词搜索日程
     *
     * @param keyword 搜索关键词
     * @return 匹配的日程列表
     */
    @GetMapping("/search")
    public R<List<Schedule>> search(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.error("请输入搜索关键词");
        }
        return R.success(scheduleService.searchByTitle(keyword));
    }
}
