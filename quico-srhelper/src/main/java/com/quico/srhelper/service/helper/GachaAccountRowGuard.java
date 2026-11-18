package com.quico.srhelper.service.helper;

import java.util.function.Consumer;

import com.quico.common.utils.StringUtils;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.enums.ViolationTypeEnum;
import com.quico.srhelper.exception.UploadValidateException;

/**
 * 解析过程中的游戏账号守卫（逐行回调）
 *
 * 在解析读到第一行时立即：
 * 1. 提取基准 uid，uid 为空判轻度格式错误；
 * 2. 做账号归属预检，已被他人绑定/超绑定上限时立刻中断解析（业务拦截，不计信用）。
 *
 * 后续每一行只校验 uid 与基准一致，发现多账号拼接立即判重度违规。
 * 账号校验不依赖文件级防伪的逃生舱，始终生效。
 *
 * @author quico
 */
public class GachaAccountRowGuard implements Consumer<SrGachaRecord>
{
    private final GachaAccountBindingService bindingService;

    private final Long userId;

    private String baseUid;

    public GachaAccountRowGuard(GachaAccountBindingService bindingService, Long userId)
    {
        this.bindingService = bindingService;
        this.userId = userId;
    }

    @Override
    public void accept(SrGachaRecord record)
    {
        String uid = record == null ? null : StringUtils.trim(record.getUid());
        if (StringUtils.isEmpty(uid))
        {
            throw new UploadValidateException(ViolationTypeEnum.FORMAT_ERROR, "存在 uid 为空的抽卡记录");
        }

        if (baseUid == null)
        {
            baseUid = uid;
            bindingService.preCheck(uid, userId);
            return;
        }

        if (!baseUid.equals(uid))
        {
            throw new UploadValidateException(ViolationTypeEnum.MULTI_UID_ATTACK,
                    "文件包含多个游戏账号： " + baseUid + "、" + uid);
        }
    }

    /**
     * 首行提取到的基准 uid；文件为空时返回 null（空文件由解析阶段判定）
     */
    public String getBaseUid()
    {
        return baseUid;
    }
}
