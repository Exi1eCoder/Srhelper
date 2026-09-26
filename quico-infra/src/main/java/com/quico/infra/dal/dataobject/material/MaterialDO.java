package com.quico.infra.dal.dataobject.material;

import com.quico.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 素材库 DO
 *
 * @author quico
 */
@TableName("sr_material")
@KeySequence("sr_material_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 类型1、图片；2、视频
     */
    private String type;
    /**
     * 分组ID
     */
    private String groupId;
    /**
     * 素材名
     */
    private String name;
    /**
     * 素材链接
     */
    private String url;

}
