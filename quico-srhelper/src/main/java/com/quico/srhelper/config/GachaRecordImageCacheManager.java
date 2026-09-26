package com.quico.srhelper.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrGachaItem;
import com.quico.srhelper.domain.SrLightCones;
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

    public void refresh() {
        log.info("开始刷新卡池项目图片缓存...");
        Cache<String, String> newCache = Caffeine.newBuilder()
                .maximumSize(500)
                .build();

        Map<Long, String> characterAvatarMap = srCharacterMapper.selectSrCharacterList(new SrCharacter())
                .stream()
                .filter(c -> c.getAvatar() != null)
                .collect(Collectors.toMap(SrCharacter::getId, SrCharacter::getAvatar, (a, b) -> a));

        Map<Long, String> lightconeImageMap = srLightConesMapper.selectSrLightConesList(new SrLightCones())
                .stream()
                .filter(l -> l.getImage() != null)
                .collect(Collectors.toMap(SrLightCones::getId, SrLightCones::getImage, (a, b) -> a));

        List<SrGachaItem> items = srGachaItemMapper.selectSrGachaItemList(new SrGachaItem());
        int count = 0;
        int skip = 0;
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
        log.info("卡池项目图片缓存刷新完成，加载 {} 条，跳过 {} 条", count, skip);
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