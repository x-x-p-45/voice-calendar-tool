package com.calendar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 语音日历工具 — 项目启动入口
 * <p>
 * 一键启动即可运行，访问地址：http://localhost:8080
 */
@SpringBootApplication
@MapperScan("com.calendar.mapper")
public class VoiceCalendarApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceCalendarApplication.class, args);
        System.out.println("========================================");
        System.out.println("  语音日历工具启动成功！");
        System.out.println("  访问地址：http://localhost:8080");
        System.out.println("  接口测试：");
        System.out.println("    GET  /schedule/list         查询所有日程");
        System.out.println("    POST /schedule/voiceAdd      语音创建日程");
        System.out.println("    GET  /schedule/listByRange   按日期范围查询");
        System.out.println("    DELETE /schedule/delete/{id} 删除日程");
        System.out.println("    PUT  /schedule/remind/{id}   更新提醒状态");
        System.out.println("========================================");
    }
}
