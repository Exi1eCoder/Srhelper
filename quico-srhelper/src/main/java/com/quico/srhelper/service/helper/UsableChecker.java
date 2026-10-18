package com.quico.srhelper.service.helper;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.quico.common.exception.BusinessException;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.enums.ItemAscensionTypeEnum;
import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;

import lombok.RequiredArgsConstructor;

/**
 * 材料绑定完成度门禁（动态推导，不在主表落 is_active 字段）
 *
 * 判定口径：
 * 1. 角色可用：sr_character_material_bind 中该 character_id 的绑定行数 = 8
 * 2. 光锥可用：sr_lightcone_material_bind 中该 lightcone_id 的绑定行数 = 6
 * 3. 材料可用：CAL（拟造花萼）/TRA（世界掉落）类型必须属于完整系列
 *    （series_id 指向的二星锚点行，其同系列同类型行数 = 3）
 *
 * 角色与光锥共用，图鉴/列表接口在缓存构建时用本类标记 usable，计算与库存写入时用本类做兜底校验。
 *
 * @author quico
 */
@Component
@RequiredArgsConstructor
public class UsableChecker {

    private final SrCharacterMaterialBindMapper characterBindMapper;

    private final SrLightconeMaterialBindMapper lightconeBindMapper;

    private final SrItemMapper itemMapper;

    /**
     * 查询材料绑定完整的角色ID集合
     */
    public Set<Long> getUsableCharacterIds() {
        return toSet(characterBindMapper.selectUsableCharacterIds());
    }

    /**
     * 查询材料绑定完整的光锥ID集合
     */
    public Set<Long> getUsableLightconeIds() {
        return toSet(lightconeBindMapper.selectUsableLightconeIds());
    }

    /**
     * 查询高阶素材绑定完整的系列锚点ID集合（二星 CAL/TRA 行，且同系列同类型共3行）
     */
    public Set<Long> getCompleteSeriesIds() {
        return toSet(itemMapper.selectCompleteSeriesIds());
    }

    /**
     * 判断单个材料是否可用
     * 非 CAL/TRA 类型恒可用；CAL/TRA 必须属于完整系列
     *
     * @param item             材料
     * @param completeSeriesIds 完整系列锚点ID集合（批量场景由调用方传入，避免逐条查库）
     */
    public boolean isItemUsable(SrItem item, Set<Long> completeSeriesIds) {
        if (item == null) {
            return false;
        }
        if (!isSeriesAscensionType(item.getAscensionType())) {
            return true;
        }
        return item.getSeriesId() != null && completeSeriesIds.contains(item.getSeriesId());
    }

    /**
     * 角色不可用时抛业务异常（422），用于计算接口兜底
     */
    public void assertCharacterUsable(Long characterId) {
        if (characterId == null || !getUsableCharacterIds().contains(characterId)) {
            throw BusinessException.unprocessable("该角色材料绑定未完成，暂不支持养成计算");
        }
    }

    /**
     * 光锥不可用时抛业务异常（422），用于计算接口兜底
     */
    public void assertLightconeUsable(Long lightconeId) {
        if (lightconeId == null || !getUsableLightconeIds().contains(lightconeId)) {
            throw BusinessException.unprocessable("该光锥材料绑定未完成，暂不支持养成计算");
        }
    }

    /**
     * 按 usable 标记过滤列表
     * 图鉴页/后台：onlyUsable 不传或 false，返回全量（前端据 usable 自行置灰）
     * 选择页/我的XX：onlyUsable=true，仅保留材料绑定完整的数据
     *
     * @param list        已标记 usable 的数据列表
     * @param onlyUsable  是否仅返回可用数据
     * @param usableGetter 列表元素的 usable 取值方法
     * @return 过滤后的列表（onlyUsable 为 false 时原样返回）
     */
    public static <T> List<T> filterUsable(List<T> list, Boolean onlyUsable, Function<T, Boolean> usableGetter) {
        if (list == null || !Boolean.TRUE.equals(onlyUsable)) {
            return list;
        }
        return list.stream()
                .filter(item -> Boolean.TRUE.equals(usableGetter.apply(item)))
                .collect(Collectors.toList());
    }

    /**
     * 判断材料是否为需要系列完整的晋升素材类型（CAL/TRA），
     * 与 MaterialBindExpander.isExpandable 保持同一口径，避免未知类型触发空指针
     */
    private boolean isSeriesAscensionType(String ascensionType) {
        if (ascensionType == null) {
            return false;
        }
        String type = ascensionType.toUpperCase();
        return ItemAscensionTypeEnum.TRA.getCode().equals(type)
                || ItemAscensionTypeEnum.CAL.getCode().equals(type);
    }

    private Set<Long> toSet(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(ids);
    }
}
