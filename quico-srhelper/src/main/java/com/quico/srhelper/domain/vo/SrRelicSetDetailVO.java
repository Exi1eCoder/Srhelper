package com.quico.srhelper.domain.vo;

import java.util.List;

import com.quico.srhelper.domain.SrRelicSet;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 遗器套装详情VO
 * 在套装基本信息基础上带出该套装下的遗器部件
 *
 * @author quico
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SrRelicSetDetailVO extends SrRelicSet {

    private static final long serialVersionUID = 1L;

    /** 套装下的遗器部件（仅五星） */
    private List<SrRelicPieceVO> relics;
}
