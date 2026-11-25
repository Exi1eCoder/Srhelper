package com.quico.srhelper.domain.vo;

import lombok.Data;

/**
 * 我的角色下拉选项
 * 
 * @author quico
 */
@Data
public class SrUserCharacterOptionVO
{
    /** 持有记录ID（sr_user_character.id） */
    private Long id;

    /** 角色ID（sr_character.id） */
    private Long characterId;

    /** 角色名称 */
    private String characterName;
}
