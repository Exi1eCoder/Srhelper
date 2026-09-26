package com.quico.infra.controller.admin.material.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 素材库 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialRespVO extends MaterialBaseVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "编号", required = true, example = "9920")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
