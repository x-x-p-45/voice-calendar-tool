package com.calendar.mapper;

import com.calendar.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 日程数据访问层
 * <p>
 * 使用 MyBatis 注解方式操作 schedule 表
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 新增日程
     *
     * @param schedule 日程实体
     * @return 影响行数
     */
    @Insert("INSERT INTO schedule(event_title, event_content, schedule_time, is_remind) " +
            "VALUES(#{eventTitle}, #{eventContent}, #{scheduleTime}, #{isRemind})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertSchedule(Schedule schedule);

    /**
     * 查询所有日程（按时间倒序）
     *
     * @return 日程列表
     */
    @Select("SELECT * FROM schedule ORDER BY schedule_time DESC")
    List<Schedule> selectAll();

    /**
     * 根据 ID 查询日程
     *
     * @param id 日程ID
     * @return 日程实体，不存在返回 null
     */
    @Select("SELECT * FROM schedule WHERE id = #{id}")
    Schedule selectById(Integer id);

    /**
     * 按日期范围查询日程
     *
     * @param start 起始时间
     * @param end   结束时间
     * @return 日程列表
     */
    @Select("SELECT * FROM schedule WHERE schedule_time BETWEEN #{start} AND #{end} ORDER BY schedule_time ASC")
    List<Schedule> selectByDateRange(@Param("start") Date start, @Param("end") Date end);

    /**
     * 根据 ID 删除日程
     *
     * @param id 日程ID
     * @return 影响行数
     */
    @Delete("DELETE FROM schedule WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 更新日程提醒状态
     *
     * @param id       日程ID
     * @param isRemind 提醒状态（0/1）
     * @return 影响行数
     */
    @Update("UPDATE schedule SET is_remind = #{isRemind} WHERE id = #{id}")
    int updateRemindStatus(@Param("id") Integer id, @Param("isRemind") Integer isRemind);

    /**
     * 批量删除日程
     *
     * @param ids 日程 ID 列表
     * @return 删除行数
     */
    @Delete("<script>" +
            "DELETE FROM schedule WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("ids") List<Integer> ids);

    /**
     * 批量更新提醒状态
     *
     * @param ids      日程 ID 列表
     * @param isRemind 提醒状态（0/1）
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE schedule SET is_remind = #{isRemind} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateRemind(@Param("ids") List<Integer> ids, @Param("isRemind") Integer isRemind);

    /**
     * 根据标题模糊搜索日程
     *
     * @param keyword 搜索关键词
     * @return 匹配的日程列表
     */
    @Select("SELECT * FROM schedule WHERE event_title LIKE CONCAT('%', #{keyword}, '%') ORDER BY schedule_time DESC")
    List<Schedule> searchByTitle(@Param("keyword") String keyword);
}
