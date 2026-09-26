package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.mapper.SrGachaItemMapper;
import com.quico.srhelper.domain.SrGachaItem;
import com.quico.srhelper.service.ISrGachaItemService;

/**
 * 卡池项目Service业务层处理
 * 
 * @author quico
 * @date 2026-08-10
 */
@Service
public class SrGachaItemServiceImpl implements ISrGachaItemService 
{
    @Autowired
    private SrGachaItemMapper srGachaItemMapper;

    /**
     * 查询卡池项目
     * 
     * @param id 卡池项目主键
     * @return 卡池项目
     */
    @Override
    public SrGachaItem selectSrGachaItemById(Long id)
    {
        return srGachaItemMapper.selectSrGachaItemById(id);
    }

    /**
     * 查询卡池项目列表
     * 
     * @param srGachaItem 卡池项目
     * @return 卡池项目
     */
    @Override
    public List<SrGachaItem> selectSrGachaItemList(SrGachaItem srGachaItem)
    {
        return srGachaItemMapper.selectSrGachaItemList(srGachaItem);
    }

    /**
     * 新增卡池项目
     * 
     * @param srGachaItem 卡池项目
     * @return 结果
     */
    @Override
    public int insertSrGachaItem(SrGachaItem srGachaItem)
    {
        srGachaItem.setCreateTime(DateUtils.getNowDate());
        return srGachaItemMapper.insertSrGachaItem(srGachaItem);
    }

    /**
     * 修改卡池项目
     * 
     * @param srGachaItem 卡池项目
     * @return 结果
     */
    @Override
    public int updateSrGachaItem(SrGachaItem srGachaItem)
    {
        srGachaItem.setUpdateTime(DateUtils.getNowDate());
        return srGachaItemMapper.updateSrGachaItem(srGachaItem);
    }

    /**
     * 批量删除卡池项目
     * 
     * @param ids 需要删除的卡池项目主键
     * @return 结果
     */
    @Override
    public int deleteSrGachaItemByIds(Long[] ids)
    {
        return srGachaItemMapper.deleteSrGachaItemByIds(ids);
    }

    /**
     * 删除卡池项目信息
     * 
     * @param id 卡池项目主键
     * @return 结果
     */
    @Override
    public int deleteSrGachaItemById(Long id)
    {
        return srGachaItemMapper.deleteSrGachaItemById(id);
    }
}
