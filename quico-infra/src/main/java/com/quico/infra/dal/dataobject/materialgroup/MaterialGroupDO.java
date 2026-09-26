package com.quico.infra.dal.dataobject.materialgroup;

import com.quico.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 素材分组 DO
 *
 * @author quico
 */
@TableName("sr_material_group")
@KeySequence("sr_material_group_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialGroupDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 分组名
     */
    private String name;

}
