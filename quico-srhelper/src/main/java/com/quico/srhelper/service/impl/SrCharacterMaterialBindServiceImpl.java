package com.quico.srhelper.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrCharacterMaterialBind;
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.dto.BindImportResult;
import com.quico.srhelper.domain.dto.SrCharacterMaterialBindExcel;
import com.quico.srhelper.domain.dto.SrCharacterSaveDTO;
import com.quico.srhelper.domain.enums.ItemAscensionTypeEnum;
import com.quico.srhelper.mapper.SrCharacterMapper;
import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.service.ISrCharacterMaterialBindService;
import com.quico.srhelper.service.ISrCharacterService;

/**
 * 角色材料绑定Service业务层处理
 *
 * @author quico
 * @date 2026-06-14
 */
@Slf4j
@Service
public class SrCharacterMaterialBindServiceImpl implements ISrCharacterMaterialBindService
{
    @Autowired
    private SrCharacterMaterialBindMapper srCharacterMaterialBindMapper;

    @Autowired
    private SrCharacterMapper srCharacterMapper;

    @Autowired
    private SrItemMapper srItemMapper;

    /**
     * 复用角色统一保存逻辑（含 TRA/CAL 高阶素材扩展、晋升材料重新生成）；
     * 使用 @Lazy 避免潜在的循环依赖初始化问题
     */
    @Lazy
    @Autowired
    private ISrCharacterService srCharacterService;

    /** 透视列顺序：世界掉落、拟造花萼、凝滞虚影、历战余响 */
    private static final ItemAscensionTypeEnum[] COLUMNS = {
            ItemAscensionTypeEnum.TRA,
            ItemAscensionTypeEnum.CAL,
            ItemAscensionTypeEnum.SS,
            ItemAscensionTypeEnum.EOW
    };

    /**
     * 查询角色材料绑定
     *
     * @param id 角色材料绑定主键
     * @return 角色材料绑定
     */
    @Override
    public SrCharacterMaterialBind selectSrCharacterMaterialBindById(Long id)
    {
        return srCharacterMaterialBindMapper.selectSrCharacterMaterialBindById(id);
    }

    /**
     * 查询角色材料绑定列表
     *
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 角色材料绑定
     */
    @Override
    public List<SrCharacterMaterialBind> selectSrCharacterMaterialBindList(SrCharacterMaterialBind srCharacterMaterialBind)
    {
        return srCharacterMaterialBindMapper.selectSrCharacterMaterialBindList(srCharacterMaterialBind);
    }

    /**
     * 新增角色材料绑定
     *
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 结果
     */
    @Override
    public int insertSrCharacterMaterialBind(SrCharacterMaterialBind srCharacterMaterialBind)
    {
        srCharacterMaterialBind.setCreateTime(DateUtils.getNowDate());
        return srCharacterMaterialBindMapper.insertSrCharacterMaterialBind(srCharacterMaterialBind);
    }

    /**
     * 修改角色材料绑定
     *
     * @param srCharacterMaterialBind 角色材料绑定
     * @return 结果
     */
    @Override
    public int updateSrCharacterMaterialBind(SrCharacterMaterialBind srCharacterMaterialBind)
    {
        srCharacterMaterialBind.setUpdateTime(DateUtils.getNowDate());
        return srCharacterMaterialBindMapper.updateSrCharacterMaterialBind(srCharacterMaterialBind);
    }

    /**
     * 批量删除角色材料绑定
     *
     * @param ids 需要删除的角色材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrCharacterMaterialBindByIds(Long[] ids)
    {
        return srCharacterMaterialBindMapper.deleteSrCharacterMaterialBindByIds(ids);
    }

    /**
     * 删除角色材料绑定信息
     *
     * @param id 角色材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrCharacterMaterialBindById(Long id)
    {
        return srCharacterMaterialBindMapper.deleteSrCharacterMaterialBindById(id);
    }

    /**
     * 查询角色材料绑定透视数据
     * 数据库中每个角色存 8 行（TRA×3 + CAL×3 + SS + EOW），导出时聚合成一行：
     * TRA/CAL 取同类型中稀有度最低（二星基础素材）的材料名，SS/EOW 直接取该行材料名
     */
    @Override
    public List<SrCharacterMaterialBindExcel> selectBindExcelList(SrCharacterMaterialBind query)
    {
        List<SrCharacterMaterialBind> binds = srCharacterMaterialBindMapper.selectSrCharacterMaterialBindList(query);

        // 按角色分组，保持列表原有顺序
        Map<Long, List<SrCharacterMaterialBind>> groups = new LinkedHashMap<>();
        for (SrCharacterMaterialBind bind : binds)
        {
            groups.computeIfAbsent(bind.getCharacterId(), k -> new ArrayList<>()).add(bind);
        }

        List<SrCharacterMaterialBindExcel> result = new ArrayList<>();
        for (List<SrCharacterMaterialBind> characterBinds : groups.values())
        {
            // 各类型代表行：TRA/CAL 取稀有度最低（二星基础素材），SS/EOW 取首行
            Map<String, SrCharacterMaterialBind> representative = new LinkedHashMap<>();
            for (SrCharacterMaterialBind bind : characterBinds)
            {
                String type = bind.getItemAscensionType();
                if (type == null)
                {
                    continue;
                }
                String key = type.toUpperCase();
                SrCharacterMaterialBind current = representative.get(key);
                if (current == null || isLowerRarity(bind, current))
                {
                    representative.put(key, bind);
                }
            }

            SrCharacterMaterialBindExcel excel = new SrCharacterMaterialBindExcel();
            excel.setCharacterName(characterBinds.get(0).getCharacterName());
            if (representative.containsKey("TRA"))
            {
                excel.setTraItemName(representative.get("TRA").getItemName());
            }
            if (representative.containsKey("CAL"))
            {
                excel.setCalItemName(representative.get("CAL").getItemName());
            }
            if (representative.containsKey("SS"))
            {
                excel.setSsItemName(representative.get("SS").getItemName());
            }
            if (representative.containsKey("EOW"))
            {
                excel.setEowItemName(representative.get("EOW").getItemName());
            }
            result.add(excel);
        }
        return result;
    }

    /**
     * candidate 稀有度低于 current（null 稀有度视为最大，不替换）
     */
    private boolean isLowerRarity(SrCharacterMaterialBind candidate, SrCharacterMaterialBind current)
    {
        if (candidate.getRarityLevel() == null)
        {
            return false;
        }
        if (current.getRarityLevel() == null)
        {
            return true;
        }
        return candidate.getRarityLevel() < current.getRarityLevel();
    }

    /**
     * 导入角色材料绑定透视数据
     * 一行一个角色，四列填材料名称（TRA/CAL 填二星基础素材名，按 seriesId 自动扩展高阶素材）。
     * 校验通过后复用 ISrCharacterService.saveAll 保存，与页面编辑走同一条链路。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BindImportResult importBindExcel(List<SrCharacterMaterialBindExcel> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            throw new RuntimeException("导入数据不能为空！");
        }

        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        List<String> failedCharacters = new ArrayList<>();
        int successNum = 0;
        int updateNum = 0;
        int skipNum = 0;

        for (SrCharacterMaterialBindExcel row : list)
        {
            String characterName = trimToEmpty(row.getCharacterName());
            if (StringUtils.isEmpty(characterName))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、角色名称不能为空");
                log.info("材料绑定导入失败：角色名称为空，行数据 {}", row);
                continue;
            }

            // 按名称精确查询角色：不存在 / 同名多版本 都无法确定目标
            List<SrCharacter> characters = srCharacterMapper.selectSrCharacterListByName(characterName);
            if (characters.isEmpty())
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、角色[").append(characterName).append("]不存在，请先导入角色");
                failedCharacters.add(characterName);
                log.info("材料绑定导入失败：角色[{}]不存在，行数据 {}", characterName, row);
                continue;
            }
            if (characters.size() > 1)
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、角色[").append(characterName).append("]存在多个实装版本记录，无法唯一确定");
                failedCharacters.add(characterName);
                log.info("材料绑定导入失败：角色[{}]存在多个实装版本记录，行数据 {}", characterName, row);
                continue;
            }
            SrCharacter character = characters.get(0);

            // 解析四列材料（空单元格表示该类型不绑定）
            List<SrCharacterMaterialBind> materialBinds = new ArrayList<>();
            boolean rowValid = true;
            for (ItemAscensionTypeEnum column : COLUMNS)
            {
                String itemName = getCellValue(row, column);
                if (StringUtils.isEmpty(itemName))
                {
                    continue;
                }
                SrItem item = srItemMapper.selectSrItemByName(itemName);
                if (item == null)
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum)
                            .append("、角色[").append(characterName).append("]")
                            .append(column.getDesc()).append("材料[").append(itemName).append("]不存在，请先导入材料");
                    failedCharacters.add(characterName);
                    log.info("材料绑定导入失败：角色[{}]{}材料[{}]不存在，行数据 {}",
                            characterName, column.getDesc(), itemName, row);
                    rowValid = false;
                    break;
                }
                if (!column.getCode().equalsIgnoreCase(item.getAscensionType()))
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum)
                            .append("、角色[").append(characterName).append("]")
                            .append(column.getDesc()).append("列材料[").append(itemName).append("]类型不匹配");
                    failedCharacters.add(characterName);
                    log.info("材料绑定导入失败：角色[{}]{}列材料[{}]类型不匹配，行数据 {}",
                            characterName, column.getDesc(), itemName, row);
                    rowValid = false;
                    break;
                }
                materialBinds.add(SrCharacterMaterialBind.builder()
                        .characterId(character.getId())
                        .characterName(characterName)
                        .itemAscensionType(column.getCode())
                        .itemId(item.getId())
                        .seriesId(item.getSeriesId())
                        .itemName(item.getItemName())
                        .itemImage(item.getImage())
                        .rarityLevel(item.getStarLevel() != null ? item.getStarLevel().intValue() : null)
                        .build());
            }
            if (!rowValid)
            {
                continue;
            }
            if (materialBinds.isEmpty())
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、角色[").append(characterName).append("]未填写任何材料");
                failedCharacters.add(characterName);
                log.info("材料绑定导入失败：角色[{}]未填写任何材料，行数据 {}", characterName, row);
                continue;
            }

            // 已存在绑定时按 updateSupport 决定跳过或覆盖
            List<SrCharacterMaterialBind> existing = srCharacterMaterialBindMapper.selectByCharacterId(character.getId());
            if (existing != null && !existing.isEmpty())
            {
                if (!updateSupport)
                {
                    skipNum++;
                    continue;
                }
                updateNum++;
            }
            else
            {
                successNum++;
            }

            // 复用统一保存：自动扩展 TRA/CAL 高阶素材并重新生成晋升材料
            SrCharacterSaveDTO dto = new SrCharacterSaveDTO();
            dto.setChangeType("full");
            dto.setCharacter(character);
            dto.setMaterialBinds(materialBinds);
            srCharacterService.saveAll(dto);
        }

        // 构建返回消息
        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("导入完成：新增绑定 ").append(successNum).append(" 个角色");
        if (updateNum > 0)
        {
            resultMsg.append("，更新 ").append(updateNum).append(" 个角色");
        }
        if (skipNum > 0)
        {
            resultMsg.append("，跳过已存在 ").append(skipNum).append(" 个角色");
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "，" + failureNum + " 条数据格式不正确：");
            resultMsg.append(failureMsg);
        }

        BindImportResult result = new BindImportResult();
        result.setMessage(resultMsg.toString());
        result.setFailedCharacters(failedCharacters);
        return result;
    }

    /**
     * 取透视行中指定类型列的材料名称
     */
    private String getCellValue(SrCharacterMaterialBindExcel row, ItemAscensionTypeEnum column)
    {
        switch (column)
        {
            case TRA:
                return trimToEmpty(row.getTraItemName());
            case CAL:
                return trimToEmpty(row.getCalItemName());
            case SS:
                return trimToEmpty(row.getSsItemName());
            case EOW:
                return trimToEmpty(row.getEowItemName());
            default:
                return "";
        }
    }

    private String trimToEmpty(String value)
    {
        return value == null ? "" : value.trim();
    }
}
