package com.quico.srhelper.domain.vo;

import com.quico.srhelper.domain.SrLightconeAscensionTemplate;
import lombok.Data;

import java.util.List;

@Data
public class SrLightconeExpFullVO {

    private List<SrLightconeExpUpgradeVO> expUpgrades;

    private List<SrLightconeAscensionTemplate> ascensionTemplates;
}