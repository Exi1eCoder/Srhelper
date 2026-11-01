package com.quico.srhelper.service.helper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrGachaItem;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.vo.StandardInfoVO;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrGachaItemMapper;
import com.quico.srhelper.mapper.SrLightConesMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GachaRecordImageCacheManager {

    @Autowired
    private SrGachaItemMapper srGachaItemMapper;

    @Autowired
    private SrCharacterMapper srCharacterMapper;

    @Autowired
    private SrLightConesMapper srLightConesMapper;

    private final Cache<String, String> imageCache = Caffeine.newBuilder()
            .maximumSize(500)
            .build();

    /** 卡池物品常驻信息缓存：character:{keyword} -> 常驻类型与加入常驻池时间 */
    private final Cache<String, StandardInfoVO> standardCache = Caffeine.newBuilder()
            .maximumSize(500)
            .build();

    @PostConstruct
    public void init() {
        refresh();
    }

    public String getImageUrl(String itemType, String itemId) {
        if (itemType == null || itemId == null) {
            return null;
        }
        return imageCache.getIfPresent(buildKey(itemType, itemId));
    }

    /**
     * 获取卡池物品的常驻信息（歪判定用）
     * 非角色（如光锥）或未配置常驻类型的物品返回 null
     */
    public StandardInfoVO getStandardInfo(String itemType, String itemId) {
        if (itemType == null || itemId == null) {
            return null;
        }
        return standardCache.getIfPresent(buildKey(itemType, itemId));
    }

    public void refresh() {
        log.info("开始刷新卡池项目图片缓存...");
        Cache<String, String> newCache = Caffeine.newBuilder()
                .maximumSize(500)
                .build();
        Cache<String, StandardInfoVO> newStandardCache = Caffeine.newBuilder()
                .maximumSize(500)
                .build();

        List<SrCharacter> characters = srCharacterMapper.selectSrCharacterList(new SrCharacter());
        Map<Long, String> characterAvatarMap = characters
                .stream()
                .filter(c -> c.getAvatar() != null)
                .collect(Collectors.toMap(SrCharacter::getId, SrCharacter::getAvatar, (a, b) -> a));

        // 常驻类型：0-限定 1-基础常驻 2-星缘相遇，只有非限定角色参与歪判定
        Map<Long, StandardInfoVO> characterStandardMap = characters
                .stream()
                .filter(c -> c.getStandardType() != null && c.getStandardType() > 0)
                .collect(Collectors.toMap(SrCharacter::getId,
                        c -> new StandardInfoVO(c.getStandardType(), c.getStandardStartTime()), (a, b) -> a));

        Map<Long, String> lightconeImageMap = srLightConesMapper.selectSrLightConesList(new SrLightCones())
                .stream()
                .filter(l -> l.getImage() != null)
                .collect(Collectors.toMap(SrLightCones::getId, SrLightCones::getImage, (a, b) -> a));

        List<SrGachaItem> items = srGachaItemMapper.selectSrGachaItemList(new SrGachaItem());
        int count = 0;
        int skip = 0;
        int standardCount = 0;
        for (SrGachaItem item : items) {
            if (item.getKeyword() == null || item.getSrId() == null || item.getItemType() == null) {
                skip++;
                continue;
            }

            try {
                String imageUrl = null;
                Long srId = Long.parseLong(item.getSrId());
                String normalizedType = normalizeType(item.getItemType());

                if ("character".equals(normalizedType)) {
                    imageUrl = characterAvatarMap.get(srId);
                    StandardInfoVO standardInfo = characterStandardMap.get(srId);
                    if (standardInfo != null) {
                        newStandardCache.put(normalizedType + ":" + item.getKeyword(), standardInfo);
                        standardCount++;
                    }
                } else if ("lightcone".equals(normalizedType)) {
                    imageUrl = lightconeImageMap.get(srId);
                }

                if (imageUrl != null) {
                    newCache.put(normalizedType + ":" + item.getKeyword(), imageUrl);
                    count++;
                } else {
                    skip++;
                }
            } catch (NumberFormatException e) {
                log.warn("跳过无效 sr_id: keyword={}, srId={}", item.getKeyword(), item.getSrId());
                skip++;
            }
        }

        ConcurrentMap<String, String> oldMap = imageCache.asMap();
        oldMap.clear();
        imageCache.putAll(newCache.asMap());

        ConcurrentMap<String, StandardInfoVO> oldStandardMap = standardCache.asMap();
        oldStandardMap.clear();
        standardCache.putAll(newStandardCache.asMap());

        log.info("卡池项目图片缓存刷新完成，加载 {} 条，跳过 {} 条；常驻标记 {} 条", count, skip, standardCount);
    }

    private String buildKey(String itemType, String itemId) {
        return normalizeType(itemType) + ":" + itemId;
    }

    private String normalizeType(String itemType) {
        if ("角色".equals(itemType)) {
            return "character";
        }
        if ("光锥".equals(itemType)) {
            return "lightcone";
        }
        return itemType.toLowerCase();
    }
}