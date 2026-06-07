package com.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.entity.AdminLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员日志 Mapper 接口
 */
@Mapper
public interface AdminLogMapper extends BaseMapper<AdminLog> {

    /**
     * 分页查询管理员操作日志（含管理员用户名）
     *
     * @param page       分页对象
     * @param targetType 对象类型筛选（可选）
     * @param adminId    管理员ID筛选（可选）
     * @return 日志分页结果
     */
    IPage<AdminLog> selectLogListWithAdmin(Page<AdminLog> page,
                                           @Param("targetType") String targetType,
                                           @Param("adminId") Long adminId);
}