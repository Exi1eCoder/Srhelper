package com.quico.srhelper.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quico.srhelper.domain.SrUserRelic;

/**
 * 用户遗器Mapper接口
 * 
 * @author quico
 * @date 2026-11-25
 */
public interface SrUserRelicMapper
{
    /**
     * 查询用户遗器详情（含遗器/套装展示字段）
     * 
     * @param id 主键
     * @return 用户遗器
     */
    public SrUserRelic selectSrUserRelicById(Long id);

    /**
     * 查询用户遗器列表
     * 
     * @param srUserRelic 查询条件
     * @return 用户遗器集合
     */
    public List<SrUserRelic> selectSrUserRelicList(SrUserRelic srUserRelic);

    /**
     * 按主键批量查询
     * 
     * @param ids 主键集合
     * @return 用户遗器集合
     */
    public List<SrUserRelic> selectSrUserRelicByIds(Long[] ids);

    /**
     * 查询某角色某部位已装备的其他遗器
     * 
     * @param userId 用户ID
     * @param equippedBy 角色ID
     * @param slot 部位
     * @param excludeId 排除的遗器主键
     * @return 冲突遗器集合
     */
    public List<SrUserRelic> selectEquippedRelicBySlot(@Param("userId") Long userId,
            @Param("equippedBy") Long equippedBy, @Param("slot") String slot,
            @Param("excludeId") Long excludeId);

    /**
     * 新增用户遗器
     * 
     * @param srUserRelic 用户遗器
     * @return 结果
     */
    public int insertSrUserRelic(SrUserRelic srUserRelic);

    /**
     * 修改用户遗器
     * 
     * @param srUserRelic 用户遗器
     * @return 结果
     */
    public int updateSrUserRelic(SrUserRelic srUserRelic);

    /**
     * 更新锁定状态
     * 
     * @param id 主键
     * @param locked 是否锁定
     * @param updateBy 更新者
     * @param updateTime 更新时间
     * @return 结果
     */
    public int updateSrUserRelicLock(@Param("id") Long id, @Param("locked") Boolean locked,
            @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);

    /**
     * 更新装备角色
     * 
     * @param id 主键
     * @param equippedBy 角色ID，null 表示卸下
     * @param updateBy 更新者
     * @param updateTime 更新时间
     * @return 结果
     */
    public int updateSrUserRelicEquip(@Param("id") Long id, @Param("equippedBy") Long equippedBy,
            @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);

    /**
     * 批量删除用户遗器
     * 
     * @param ids 主键集合
     * @return 结果
     */
    public int deleteSrUserRelicByIds(Long[] ids);
}
