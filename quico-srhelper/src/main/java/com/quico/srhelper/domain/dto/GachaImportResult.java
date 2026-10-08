package com.quico.srhelper.domain.dto;

import lombok.Data;

/**
 * 跃迁记录导入结果
 * 历史数据补录时 needRecalc=true，前端据此轮询重算状态
 *
 * @author quico
 */
@Data
public class GachaImportResult
{
    /** 是否需要异步重算统计（补录历史数据时为 true） */
    private boolean needRecalc;

    /** 需要重算的游戏账号 uid（单次导入通常只有一个） */
    private String uid;

    /** 给用户看的结果消息 */
    private String message;

    public GachaImportResult()
    {
    }

    public GachaImportResult(boolean needRecalc, String uid, String message)
    {
        this.needRecalc = needRecalc;
        this.uid = uid;
        this.message = message;
    }
}
