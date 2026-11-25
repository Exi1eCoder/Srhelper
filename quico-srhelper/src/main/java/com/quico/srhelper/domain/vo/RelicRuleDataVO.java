package com.quico.srhelper.domain.vo;

import java.util.List;

import com.quico.srhelper.domain.SrRelic;
import com.quico.srhelper.domain.SrRelicMainAffix;
import com.quico.srhelper.domain.SrRelicSet;
import com.quico.srhelper.domain.SrRelicSetProperty;
import com.quico.srhelper.domain.SrRelicSubAffix;

import lombok.Data;

/**
 * 遗器规则数据（前端一次拉取并缓存）
 * 
 * @author quico
 */
@Data
public class RelicRuleDataVO
{
    /** 规则数据版本 */
    private String ruleVersion;

    /** 5 星遗器定义 */
    private List<SrRelic> relicList;

    /** 主属性池（组 51-56） */
    private List<SrRelicMainAffix> mainAffixList;

    /** 副属性池（组 5） */
    private List<SrRelicSubAffix> subAffixList;

    /** 套装 */
    private List<SrRelicSet> relicSetList;

    /** 套装静态属性 */
    private List<SrRelicSetProperty> relicSetPropertyList;
}
