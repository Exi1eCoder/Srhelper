package com.quico.srhelper.service;

import java.util.List;

import com.quico.srhelper.domain.SrUserRelic;
import com.quico.srhelper.domain.dto.SrUserRelicSaveDTO;

/**
 * 用户遗器Service接口
 * 
 * @author quico
 * @date 2026-11-25
 */
public interface ISrUserRelicService
{
    /**
     * 查询当前用户的遗器列表
     * 
     * @param srUserRelic 查询条件
     * @return 用户遗器集合
     */
    public List<SrUserRelic> selectSrUserRelicList(SrUserRelic srUserRelic);

    /**
     * 查询遗器详情（含副词条）
     * 
     * @param id 主键
     * @return 用户遗器
     */
    public SrUserRelic selectSrUserRelicDetail(Long id);

    /**
     * 新建或更新遗器（校验意图并重算派生字段）
     * 
     * @param dto 保存请求
     * @return 保存后的完整对象
     */
    public SrUserRelic saveSrUserRelic(SrUserRelicSaveDTO dto);

    /**
     * 批量删除遗器，已锁定不可删
     * 
     * @param ids 主键集合
     * @return 结果
     */
    public int deleteSrUserRelicByIds(Long[] ids);

    /**
     * 切换锁定状态
     * 
     * @param id 主键
     * @param locked 是否锁定
     * @return 结果
     */
    public int updateSrUserRelicLock(Long id, Boolean locked);

    /**
     * 装备到角色或卸下
     * 
     * @param id 主键
     * @param equippedBy 角色ID，null 表示卸下
     * @return 结果
     */
    public int updateSrUserRelicEquip(Long id, Long equippedBy);
}
