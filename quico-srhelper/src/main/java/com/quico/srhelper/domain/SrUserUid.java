package com.quico.srhelper.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户与游戏账号绑定对象 sr_user_uid
 *
 * 先到先得：uid 唯一，一个游戏账号只能绑定一个系统用户，
 * 并发抢注由数据库唯一索引裁决
 *
 * @author quico
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrUserUid implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 游戏内账号UID */
    private String uid;

    /** 绑定的系统用户ID */
    private Long userId;

    /** 最近一次管理员转移时间，用于30天冻结期 */
    private Date transferTime;

    /** 首次绑定时间 */
    private Date createTime;

    /** 最后修改时间 */
    private Date updateTime;
}
