package com.quico.srhelper.domain.dto;

import java.util.List;
import lombok.Data;

/**
 * 角色材料绑定导入结果
 * message 为人读的统计摘要；failedCharacters 记录所有失败角色名，供前端直接提示
 *
 * @author quico
 * @date 2026-09-28
 */
@Data
public class BindImportResult
{
    /** 人读的消息摘要 */
    private String message;

    /** 导入失败的角色名列表（角色不存在 / 材料不存在 / 材料类型不匹配等） */
    private List<String> failedCharacters;
}
