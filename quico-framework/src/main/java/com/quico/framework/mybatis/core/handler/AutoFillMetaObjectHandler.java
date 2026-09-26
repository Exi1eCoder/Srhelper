package com.quico.framework.mybatis.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.quico.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * MyBatis Plus 自动填充处理器
 * 用于自动填充 createTime、updateTime、createBy、updateBy 等字段
 */
@Slf4j
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充时间（兼容 Date 和 LocalDateTime）
        Date now = new Date();
        this.strictInsertFill(metaObject, "createTime", () -> now, Date.class);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", () -> now, Date.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);

        // 自动填充操作人（同时兼容 createBy/updateBy 和 creator/updater）
        String user = getCurrentUsername();
        this.strictInsertFill(metaObject, "createBy", () -> user, String.class);
        this.strictInsertFill(metaObject, "updateBy", () -> user, String.class);
        this.strictInsertFill(metaObject, "creator", () -> user, String.class);
        this.strictInsertFill(metaObject, "updater", () -> user, String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充时间（兼容 Date 和 LocalDateTime）
        Date now = new Date();
        this.strictUpdateFill(metaObject, "updateTime", () -> now, Date.class);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);

        // 自动填充操作人
        String user = getCurrentUsername();
        this.strictUpdateFill(metaObject, "updateBy", () -> user, String.class);
        this.strictUpdateFill(metaObject, "updater", () -> user, String.class);
    }

    /**
     * 获取当前登录用户 ID，未登录时返回 "0"
     */
    private String getCurrentUsername() {
        return SecurityUtils.getUserId().toString();
    }
}
