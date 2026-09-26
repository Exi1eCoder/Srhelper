package com.quico.infra.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.quico.common.annotation.Excel;
import com.quico.common.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * 用户签到记录对象 quico_user_sign_in
 * 
 * @author quico
 * @date 2026-08-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuicoUserSignIn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 签到日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签到日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date signDate;

    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签到时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date signTime;

    /** 是否补签(0:否,1:是) */
    @Excel(name = "是否补签(0:否,1:是)")
    private Integer isRepaired;


}
