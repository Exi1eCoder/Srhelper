package com.quico.srhelper.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户所有角色养成计算VO（每个角色单独返回材料，不含总计）
 */
@Data
public class SrCharactersCultivationVO {

    /** 角色列表，每个角色内嵌自身所需材料 */
    private List<CharacterInfo> characters;

    /** 角色信息（含该角色所需材料） */
    @Data
    public static class CharacterInfo {
        private Long userCharacterId;
        private Long characterId;
        private String characterName;
        private String characterImage;
        private Integer currentLevel;
        private Integer targetLevel;
        /** 是否已培养完成（到目标等级无需任何材料） */
        private Boolean isMaxLevel;
        /** 该角色所需材料 */
        private List<SrCharacterCultivationVO.MaterialRequirement> materials;
    }
}