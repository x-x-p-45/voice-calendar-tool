package com.calendar.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 语音日程实体类
 * <p>
 * 对应数据库 schedule 表，映射日程信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    /** 主键ID */
    private Integer id;

    /** 日程标题 */
    private String eventTitle;

    /** 日程详细内容 */
    private String eventContent;

    /** 日程执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date scheduleTime;

    /** 是否提醒：0-不提醒，1-提醒 */
    private Integer isRemind;

    /** 记录创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
