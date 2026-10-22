package com.quico.srhelper.domain.enums;

import lombok.Getter;

/**
 * 抽卡记录上传违规类型
 * isMalicious = 0 轻度（手滑/格式错），isMalicious = 1 重度（恶意篡改/拼接）
 *
 * @author quico
 */
@Getter
public enum ViolationTypeEnum
{
    /** 文件内容为空 */
    FILE_EMPTY(0, "文件内容为空"),
    /** 文件后缀/大小/魔数不符合要求 */
    FORMAT_ERROR(0, "文件格式错误"),
    /** Excel/CSV 解析失败 */
    PARSE_ERROR(0, "文件解析失败"),
    /** 数据行数超过上限 */
    EXCEED_LIMIT(0, "导入数据超过上限"),
    /** 出现字典外的卡池类型 */
    UNKNOWN_GACHA_TYPE(0, "未知卡池类型"),
    
    /** 首条记录账号/流水号基准字段不自洽 */
    UID_MISMATCH_BASE(1, "首条记录基准信息异常"),
    /** 流水号尾部与首条基准不一致（拼接/篡改） */
    DATA_TAMPERED_TAIL(1, "抽卡流水号尾部被篡改"),
    /** 流水号内嵌小时数与抽卡时间不一致 */
    DATA_TAMPERED_TIME(1, "抽卡流水号与抽卡时间不一致"),
    /** 同一文件出现多个游戏账号 */
    MULTI_UID_ATTACK(1, "文件包含多个游戏账号");

    private final Integer isMalicious;

    private final String desc;

    ViolationTypeEnum(Integer isMalicious, String desc)
    {
        this.isMalicious = isMalicious;
        this.desc = desc;
    }
}
