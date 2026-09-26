package com.quico.srhelper.domain.dto;

import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.SrLightconeMaterialBind;
import lombok.Data;

import java.util.List;

/**
 * 光锥保存/更新请求DTO
 */
@Data
public class LightConeSaveDTO {

    /** 更新类型：simple=仅基本信息，full=完整保存（含材料绑定和晋升材料生成） */
    private String changeType;

    /** 光锥基本信息 */
    private SrLightCones lightCone;

    /** 材料绑定列表 */
    private List<SrLightconeMaterialBind> materialBinds;
}
