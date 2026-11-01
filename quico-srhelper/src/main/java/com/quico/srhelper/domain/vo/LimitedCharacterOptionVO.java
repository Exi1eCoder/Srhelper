package com.quico.srhelper.domain.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 可选限定角色（standard_type=0）下拉选项
 * 仅用于“添加星缘相遇角色”弹窗，不含其它字段
 *
 * @author quico
 */
@Data
public class LimitedCharacterOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    private Long id;

    /** 角色名字 */
    private String characterName;

    /** 角色头像 */
    private String avatar;

    /** 实装版本（便于前端提示角色来源版本） */
    private String releaseVersion;
}
