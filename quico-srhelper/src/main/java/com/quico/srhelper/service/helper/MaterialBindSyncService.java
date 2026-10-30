package com.quico.srhelper.service.helper;

import java.util.List;

import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色材料绑定同步服务
 * 负责保持 sr_character_material_bind 表与主表的数据一致性
 * 
 * 功能：
 * 1. 应用层同步：实时同步角色名称和物品信息
 * 2. 定时任务同步：由后台定时任务 sys_job 每天凌晨 2 点执行兜底同步
 * 3. 手动触发同步：用于修复数据
 */
@Slf4j
@Service("materialBindSyncTask")
public class MaterialBindSyncService {

    @Autowired
    private SrCharacterMaterialBindMapper materialBindMapper;

    @Autowired
    private SrLightconeMaterialBindMapper lightconeMaterialBindMapper;

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
     * 调度由后台定时任务的 sys_job 配置驱动，调用目标为 materialBindSyncTask.syncAllData()
     * （建议每天凌晨 2 点执行一次）
     * 先查重再更新：只更新与主表不一致的绑定记录，不做无差别全量更新
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncAllData() {
        log.info("开始执行材料绑定表同步任务...");
        long startTime = System.currentTimeMillis();

        int characterSyncCount = syncMismatchedCharacters();
        int itemSyncCount = syncMismatchedItems();
        if (characterSyncCount == 0 && itemSyncCount == 0) {
            log.info("材料绑定表同步任务完成，未发现不一致数据");
            return;
        }

        long endTime = System.currentTimeMillis();
        log.info("材料绑定表同步任务完成，同步角色{}个，同步物品{}个，耗时{}ms", 
                characterSyncCount, itemSyncCount, endTime - startTime);
    }

    /**
     * 同步角色名称与主表不一致的绑定记录
     * 先由 SQL 查出不一致的角色（按角色去重），再按角色ID批量更新其全部绑定行
     */
    private int syncMismatchedCharacters() {
        int count = 0;
        try {
            List<SrCharacterMaterialBind> mismatched = materialBindMapper.selectNameMismatchedCharacters();
            if (mismatched == null || mismatched.isEmpty()) {
                return 0;
            }
            for (SrCharacterMaterialBind bind : mismatched) {
                materialBindMapper.updateCharacterName(bind.getCharacterId(), bind.getCharacterName());
                count++;
            }
        } catch (Exception e) {
            log.error("同步不一致的角色名称失败", e);
        }
        return count;
    }

    /**
     * 同步物品信息与主表不一致的绑定记录
     * 先由 SQL 查出不一致的物品（按物品去重，名称/图片/稀有度/系列ID 任一不一致），再按物品ID更新
     */
    private int syncMismatchedItems() {
        int count = 0;
        try {
            List<SrItem> mismatched = materialBindMapper.selectInfoMismatchedItems();
            if (mismatched == null || mismatched.isEmpty()) {
                return 0;
            }
            for (SrItem item : mismatched) {
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
            log.error("同步不一致的物品信息失败", e);
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