package com.quico.srhelper.service.impl;

import java.util.List;
import com.quico.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.srhelper.constants.RelicRuleConstants;
import com.quico.srhelper.mapper.SrRelicMainAffixMapper;
import com.quico.srhelper.mapper.SrRelicMapper;
import com.quico.srhelper.mapper.SrRelicSetMapper;
import com.quico.srhelper.mapper.SrRelicSetPropertyMapper;
import com.quico.srhelper.mapper.SrRelicSubAffixMapper;
import com.quico.srhelper.domain.SrRelic;
import com.quico.srhelper.domain.SrRelicSet;
import com.quico.srhelper.domain.vo.RelicRuleDataVO;
import com.quico.srhelper.service.ISrRelicService;

/**
 * 遗器图鉴Service业务层处理
 * 
 * @author quico
 * @date 2026-11-24
 */
@Service
public class SrRelicServiceImpl implements ISrRelicService 
{
    @Autowired
    private SrRelicMapper srRelicMapper;

    @Autowired
    private SrRelicMainAffixMapper srRelicMainAffixMapper;

    @Autowired
    private SrRelicSubAffixMapper srRelicSubAffixMapper;

    @Autowired
    private SrRelicSetMapper srRelicSetMapper;

    @Autowired
    private SrRelicSetPropertyMapper srRelicSetPropertyMapper;

    /**
     * 查询遗器图鉴
     * 
     * @param id 遗器图鉴主键
     * @return 遗器图鉴
     */
    @Override
    public SrRelic selectSrRelicById(Long id)
    {
        return srRelicMapper.selectSrRelicById(id);
    }

    /**
     * 查询遗器图鉴列表
     * 
     * @param srRelic 遗器图鉴
     * @return 遗器图鉴
     */
    @Override
    public List<SrRelic> selectSrRelicList(SrRelic srRelic)
    {
        return srRelicMapper.selectSrRelicList(srRelic);
    }

    /**
     * 查询前端所需的遗器规则数据（仅 5 星）
     * 
     * @return 规则数据
     */
    @Override
    public RelicRuleDataVO selectRelicRuleData()
    {
        RelicRuleDataVO vo = new RelicRuleDataVO();
        SrRelic query = new SrRelic();
        query.setRarity(Long.valueOf(RelicRuleConstants.RARITY_FIVE_STAR));
        vo.setRelicList(srRelicMapper.selectSrRelicList(query));
        vo.setMainAffixList(srRelicMainAffixMapper.selectByGroupIds(RelicRuleConstants.MAIN_AFFIX_GROUP_IDS));
        vo.setSubAffixList(srRelicSubAffixMapper.selectByGroupId(RelicRuleConstants.SUB_AFFIX_GROUP_ID));
        vo.setRelicSetList(srRelicSetMapper.selectSrRelicSetList(new SrRelicSet()));
        vo.setRelicSetPropertyList(srRelicSetPropertyMapper.selectSrRelicSetPropertyList());
        vo.setRuleVersion(RelicRuleConstants.RULE_VERSION);
        return vo;
    }

    /**
     * 新增遗器图鉴
     * 
     * @param srRelic 遗器图鉴
     * @return 结果
     */
    @Override
    public int insertSrRelic(SrRelic srRelic)
    {
        srRelic.setCreateTime(DateUtils.getNowDate());
        return srRelicMapper.insertSrRelic(srRelic);
    }

    /**
     * 修改遗器图鉴
     * 
     * @param srRelic 遗器图鉴
     * @return 结果
     */
    @Override
    public int updateSrRelic(SrRelic srRelic)
    {
        srRelic.setUpdateTime(DateUtils.getNowDate());
        return srRelicMapper.updateSrRelic(srRelic);
    }

    /**
     * 批量删除遗器图鉴
     * 
     * @param ids 需要删除的遗器图鉴主键
     * @return 结果
     */
    @Override
    public int deleteSrRelicByIds(Long[] ids)
    {
        return srRelicMapper.deleteSrRelicByIds(ids);
    }

    /**
     * 删除遗器图鉴信息
     * 
     * @param id 遗器图鉴主键
     * @return 结果
     */
    @Override
    public int deleteSrRelicById(Long id)
    {
        return srRelicMapper.deleteSrRelicById(id);
    }
}
