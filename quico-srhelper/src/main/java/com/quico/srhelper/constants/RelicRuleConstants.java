package com.quico.srhelper.constants;

import java.util.List;

/**
 * 遗器模拟规则常量（当前仅 5 星）
 * 
 * @author quico
 */
public final class RelicRuleConstants
{
    private RelicRuleConstants()
    {
    }

    /** 规则数据版本，规则数据变更时同步递增 */
    public static final String RULE_VERSION = "1.0.0";

    /** 仅支持的稀有度 */
    public static final int RARITY_FIVE_STAR = 5;

    /** 5 星最高等级 */
    public static final int MAX_LEVEL = 15;

    /** 初始 3 词条时可强化次数 */
    public static final int UPGRADE_MAX_INIT_THREE = 4;

    /** 初始 4 词条时可强化次数 */
    public static final int UPGRADE_MAX_INIT_FOUR = 5;

    /** 3 词条新增第 4 条的等级门槛 */
    public static final int FOURTH_SUB_UNLOCK_LEVEL = 3;

    /** 初始 3 词条 */
    public static final int SUB_COUNT_THREE = 3;

    /** 初始 4 词条 */
    public static final int SUB_COUNT_FOUR = 4;

    /** 部位：头部 */
    public static final String SLOT_HEAD = "HEAD";

    /** 部位：手部 */
    public static final String SLOT_HAND = "HAND";

    /** HEAD 固定主属性 */
    public static final String MAIN_PROPERTY_HEAD = "HPDelta";

    /** HAND 固定主属性 */
    public static final String MAIN_PROPERTY_HAND = "AttackDelta";

    /** 副属性组 ID */
    public static final String SUB_AFFIX_GROUP_ID = "5";

    /** 主属性组 ID：HEAD/HAND/BODY/FEET/PLANAR_SPHERE/LINK_ROPE */
    public static final List<String> MAIN_AFFIX_GROUP_IDS = List.of("51", "52", "53", "54", "55", "56");

    /** 来源：模拟生成 */
    public static final String ORIGIN_SIMULATED = "SIMULATED";

    /** 来源：外部导入未编辑 */
    public static final String ORIGIN_IMPORTED = "IMPORTED";

    /** 来源：外部导入后编辑 */
    public static final String ORIGIN_IMPORTED_MODIFIED = "IMPORTED_MODIFIED";

    /** 合法来源集合 */
    public static final List<String> ORIGINS = List.of(ORIGIN_SIMULATED, ORIGIN_IMPORTED, ORIGIN_IMPORTED_MODIFIED);

    /**
     * 副词条档位数量：档位从 1 开始，数量为 step_num + 1
     * 
     * @param stepNum sr_relic_sub_affix.step_num
     * @return 档位数量
     */
    public static int getTierCount(Integer stepNum)
    {
        return (stepNum == null ? 0 : stepNum) + 1;
    }

    /**
     * 可强化次数上限：3 词条 4 次，4 词条 5 次
     */
    public static int getUpgradeLimit(int initialSubCount)
    {
        return initialSubCount == SUB_COUNT_THREE ? UPGRADE_MAX_INIT_THREE : UPGRADE_MAX_INIT_FOUR;
    }

    /**
     * 当前应提交的副词条数量：3 词条在 level >= 3 时必须已补第 4 条
     */
    public static int getExpectedSubCount(int initialSubCount, int level)
    {
        if (initialSubCount == SUB_COUNT_THREE && level >= FOURTH_SUB_UNLOCK_LEVEL)
        {
            return SUB_COUNT_FOUR;
        }
        return initialSubCount;
    }
}
