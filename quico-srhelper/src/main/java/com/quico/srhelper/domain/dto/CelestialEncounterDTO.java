package com.quico.srhelper.domain.dto;

import lombok.Data;

/**
 * 批量加入星缘相遇入参
 *
 * @author quico
 */
@Data
public class CelestialEncounterDTO {

    /** 角色ID集合 */
    private Long[] ids;

    /** 版本号（如 3.2），必填，用于取该版本的实装时间 */
    private String versionCode;
}
