package com.quico.infra.controller.admin.material.vo;

import com.quico.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.quico.common.utils.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 素材库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialPageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "类型1、图片；2、视频", example = "2")
    private String type;

    @Schema(description = "分组ID", example = "21579")
    private String groupId;

    @Schema(description = "素材名", example = "srhelper")
    private String name;

    @Schema(description = "素材链接", example = "https://www.quico.com")
    private String url;

}
