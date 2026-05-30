package com.calendar.controller;

import com.calendar.common.R;
import com.calendar.entity.Schedule;
import com.calendar.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 语音文件上传控制器
 * <p>
 * 支持上传音频文件，模拟 ASR 语音识别流程，自动创建日程
 */
@RestController
@RequestMapping("/voice")
public class VoiceFileController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 上传语音文件并创建日程
     * <p>
     * 接收音频文件，模拟语音识别返回文本，再调用解析引擎创建日程。
     * 实际生产环境可替换为真实 ASR 接口（如百度语音、讯飞等）。
     *
     * @param file       音频文件（支持 wav/mp3/m4a 等格式）
     * @param voiceText  手动输入的语音文本（作为模拟 ASR 识别结果，必填）
     * @return 包含识别文本和日程详情的操作结果
     */
    @PostMapping("/upload")
    public R<Map<String, Object>> uploadVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "voiceText", defaultValue = "") String voiceText) {

        if (file.isEmpty()) {
            return R.error("请选择要上传的音频文件");
        }

        // 校验文件大小（最大 10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return R.error("文件大小不能超过 10MB");
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            if (!(lower.endsWith(".wav") || lower.endsWith(".mp3") ||
                  lower.endsWith(".m4a") || lower.endsWith(".webm") ||
                  lower.endsWith(".ogg"))) {
                return R.error("不支持的音频格式，支持：wav / mp3 / m4a / webm / ogg");
            }
        }

        // 模拟 ASR 识别：优先使用手动输入的文本，否则返回文件名作为演示
        String recognizedText;
        if (voiceText != null && !voiceText.trim().isEmpty()) {
            recognizedText = voiceText.trim();
        } else {
            recognizedText = "明天上午9点语音会议";
        }

        // 调用解析引擎创建日程
        Schedule schedule = scheduleService.analyzeVoice(recognizedText);
        scheduleService.addSchedule(schedule);

        // 构造返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileName", originalFilename);
        result.put("fileSize", file.getSize() + " bytes");
        result.put("recognizedText", recognizedText);
        result.put("schedule", schedule);

        return R.success(result);
    }
}
