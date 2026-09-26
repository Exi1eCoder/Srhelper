package com.quico.srhelper.service.helper;

import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.enums.ItemAscensionTypeEnum;
import com.quico.srhelper.mapper.SrItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 材料绑定扩展器
 * 将 TRA/CAL 类型的低阶素材通过 seriesId 扩展为同系列全部稀有度素材
 * 角色和光锥绑定共用
 */
@Component
@RequiredArgsConstructor
public class MaterialBindExpander {

    private final SrItemMapper itemMapper;

    /**
     * 判断材料类型是否需要扩展（TRA/CAL类型）
     */
    public boolean isExpandable(String itemAscensionType) {
        if (itemAscensionType == null) return false;
        String type = itemAscensionType.toUpperCase();
        return ItemAscensionTypeEnum.TRA.getCode().equals(type)
                || ItemAscensionTypeEnum.CAL.getCode().equals(type);
    }

    /**
     * 根据 seriesId 扩展为同系列所有素材
     * @param seriesId 材料系列ID
     * @return 系列中的所有 SrItem，seriesId 为空时返回空列表
     */
    public List<SrItem> expandBySeriesId(Long seriesId) {
        if (seriesId == null) return Collections.emptyList();
        List<SrItem> items = itemMapper.selectBySeriesId(seriesId);
        if (items == null) return Collections.emptyList();

        List<SrItem> result = new ArrayList<>();
        for (SrItem item : items) {
            if (item.getId() != null && item.getStarLevel() != null) {
                result.add(item);
            }
        }
        return result;
    }
}
