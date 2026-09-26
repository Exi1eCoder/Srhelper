package com.quico.srhelper.service.helper;

import java.util.List;

import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色材料绑定同步服务
 * 负责保持 sr_character_material_bind 表与主表的数据一致性
 * 
 * 功能：
 * 1. 应用层同步：实时同步角色名称和物品信息
 * 2. 定时任务同步：每天凌晨2点执行兜底同步
 * 3. 手动触发同步：用于修复数据
 */
@Slf4j
@Service
public class MaterialBindSyncService {

    @Autowired
    private SrCharacterMaterialBindMapper materialBindMapper;

    @Autowired
    private SrLightconeMaterialBindMapper lightconeMaterialBindMapper;

    @Autowired
    private SrCharacterMapper characterMapper;

    @Autowired
    private SrItemMapper itemMapper;

    /**
     * 同步角色名称到材料绑定表（应用层同步）
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncCharacterName(Long characterId, String characterName) {
        try {
            materialBindMapper.updateCharacterName(characterId, characterName);
            log.debug("同步角色名称成功: characterId={}, characterName={}", characterId, characterName);
        } catch (Exception e) {
            log.error("同步角色名称失败: characterId={}", characterId, e);
            // 不抛出异常，避免影响主业务
        }
    }

    /**
     * 同步光锥名称到材料绑定表（应用层同步）
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncLightconeName(Long lightconeId, String lightconeName) {
        try {
            lightconeMaterialBindMapper.updateLightconeName(lightconeId, lightconeName);
            log.debug("同步角色名称成功: lightconeId={}, lightconeName={}", lightconeId, lightconeName);
        } catch (Exception e) {
            log.error("同步角色名称失败: lightconeId={}", lightconeId, e);
            // 不抛出异常，避免影响主业务
        }
    }

    /**
     * 同步物品信息到材料绑定表（应用层同步）
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncItemInfo(Long itemId, String itemName, String itemImage, Integer rarityLevel) {
        try {
            materialBindMapper.updateItemInfo(itemId, itemName, itemImage, rarityLevel);
            log.debug("同步物品信息成功: itemId={}, itemName={}", itemId, itemName);
        } catch (Exception e) {
            log.error("同步物品信息失败: itemId={}", itemId, e);
            // 不抛出异常，避免影响主业务
        }
    }

    /**
     * 同步物品系列ID到材料绑定表（应用层同步）
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncItemSeriesId(Long itemId, Long seriesId) {
        try {
            materialBindMapper.updateItemSeriesId(itemId, seriesId);
            log.debug("同步物品系列ID成功: itemId={}, seriesId={}", itemId, seriesId);
        } catch (Exception e) {
            log.error("同步物品系列ID失败: itemId={}", itemId, e);
            // 不抛出异常，避免影响主业务
        }
    }

    /**
     * 定时任务：同步所有不一致的数据（兜底保障）
     * 每天凌晨2点执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncAllData() {
        log.info("开始执行材料绑定表同步任务...");
        long startTime = System.currentTimeMillis();

        int characterSyncCount = syncAllCharacters();
        int itemSyncCount = syncAllItems();

        long endTime = System.currentTimeMillis();
        log.info("材料绑定表同步任务完成，同步角色{}个，同步物品{}个，耗时{}ms", 
                characterSyncCount, itemSyncCount, endTime - startTime);
    }

    /**
     * 同步所有角色数据
     */
    private int syncAllCharacters() {
        int count = 0;
        try {
            List<SrCharacter> characters = characterMapper.selectSrCharacterList(null);
            for (SrCharacter character : characters) {
                materialBindMapper.updateCharacterName(character.getId(), character.getCharacterName());
                count++;
            }
        } catch (Exception e) {
            log.error("同步所有角色失败", e);
        }
        return count;
    }

    /**
     * 同步所有物品数据
     */
    private int syncAllItems() {
        int count = 0;
        try {
            List<SrItem> items = itemMapper.selectSrItemList(null);
            for (SrItem item : items) {
                materialBindMapper.updateItemInfo(
                        item.getId(),
                        item.getItemName(),
                        item.getImage(),
                        item.getStarLevel() != null ? item.getStarLevel().intValue() : null
                );
                if (item.getSeriesId() != null) {
                    materialBindMapper.updateItemSeriesId(item.getId(), item.getSeriesId());
                }
                count++;
            }
        } catch (Exception e) {
            log.error("同步所有物品失败", e);
        }
        return count;
    }

    /**
     * 手动触发同步（用于修复数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public void triggerSync() {
        log.info("手动触发材料绑定表同步...");
        syncAllData();
    }
}