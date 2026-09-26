package com.quico.framework.aspectj;

import java.util.Collection;
import java.util.Date;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.quico.common.core.domain.BaseEntity;
import com.quico.common.utils.SecurityUtils;

/**
 * 审计字段自动填充切面
 *
 * 通过 AOP 统一拦截 Service 层的 insert/update 方法，自动填充
 * createBy、updateBy、createTime、updateTime 四个审计字段，
 * 避免每个 Service 手动 set。
 *
 * 填充规则：
 * - 仅处理方法名匹配 insert/update/save/add/batch/import/create/edit/modify 的方法
 * - 仅填充方法参数中的 BaseEntity 实例（含 Collection 内的元素）
 * - 仅在字段为 null 时填充，不覆盖业务已手动设置的值
 * - 未登录时操作人填充为空字符串，时间字段正常填充
 *
 * @author quico
 */
@Aspect
@Order(2)
@Component
public class AuditFillAspect
{
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 切点：拦截 Service 层的写操作方法
     * 精确匹配方法名前缀，避免拦截 select/list/get 等查询方法
     */
    @Pointcut("execution(public * com.quico..service..*.insert*(..))"
            + " || execution(public * com.quico..service..*.update*(..))"
            + " || execution(public * com.quico..service..*.save*(..))"
            + " || execution(public * com.quico..service..*.add*(..))"
            + " || execution(public * com.quico..service..*.batch*(..))"
            + " || execution(public * com.quico..service..*.import*(..))"
            + " || execution(public * com.quico..service..*.create*(..))"
            + " || execution(public * com.quico..service..*.edit*(..))"
            + " || execution(public * com.quico..service..*.modify*(..))")
    public void auditPointcut()
    {

    }

    /**
     * 方法执行前填充审计字段
     */
    @Before("auditPointcut()")
    public void fillAudit(JoinPoint joinPoint)
    {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();

        FillType fillType = resolveFillType(methodName);
        if (fillType == FillType.NONE)
        {
            return;
        }

        String userId = getUserIdSafely();
        Date now = new Date();

        for (Object arg : joinPoint.getArgs())
        {
            fillEntity(arg, fillType, userId, now);
        }
    }

    /**
     * 根据方法名推断操作类型
     */
    private FillType resolveFillType(String methodName)
    {
        String lower = methodName.toLowerCase();
        if (lower.contains("insert") || lower.contains("save") || lower.contains("add")
                || lower.contains("batch") || lower.contains("import") || lower.contains("create"))
        {
            return FillType.INSERT;
        }
        if (lower.contains("update") || lower.contains("edit") || lower.contains("modify"))
        {
            return FillType.UPDATE;
        }
        return FillType.NONE;
    }

    /**
     * 递归填充实体（支持 List 等集合内的实体）
     */
    private void fillEntity(Object obj, FillType fillType, String userId, Date now)
    {
        if (obj == null)
        {
            return;
        }
        // 集合类型：递归处理内部元素
        if (obj instanceof Collection)
        {
            for (Object item : (Collection<?>) obj)
            {
                fillEntity(item, fillType, userId, now);
            }
            return;
        }
        // 只处理 BaseEntity 子类
        if (!(obj instanceof BaseEntity))
        {
            return;
        }

        BaseEntity entity = (BaseEntity) obj;
        if (fillType == FillType.INSERT)
        {
            if (entity.getCreateBy() == null)
            {
                entity.setCreateBy(userId);
            }
            if (entity.getCreateTime() == null)
            {
                entity.setCreateTime(now);
            }
        }
        if (entity.getUpdateBy() == null)
        {
            entity.setUpdateBy(userId);
        }
        if (entity.getUpdateTime() == null)
        {
            entity.setUpdateTime(now);
        }
    }

    /**
     * 获取当前登录用户 ID，未登录时返回空字符串
     */
    private String getUserIdSafely()
    {
        try
        {
            return SecurityUtils.getUserId().toString();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * 操作类型
     */
    private enum FillType
    {
        /** 新增：填充 createBy/createTime/updateBy/updateTime */
        INSERT,
        /** 修改：填充 updateBy/updateTime */
        UPDATE,
        /** 不填充 */
        NONE
    }
}
