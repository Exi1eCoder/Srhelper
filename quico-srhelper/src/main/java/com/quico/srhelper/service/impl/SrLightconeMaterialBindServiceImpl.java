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
import com.quico.srhelper.domain.SrItem;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.SrLightconeMaterialBind;
import com.quico.srhelper.domain.dto.BindImportResult;
import com.quico.srhelper.domain.dto.LightConeSaveDTO;
import com.quico.srhelper.domain.dto.SrLightconeMaterialBindExcel;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightConesMapper;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;
import com.quico.srhelper.service.ISrLightconeMaterialBindService;
import com.quico.srhelper.service.ISrLightConesService;

/**
 * 光锥材料绑定Service业务层处理
 *
 * @author quico
 * @date 2026-08-14
 */
@Slf4j
@Service
public class SrLightconeMaterialBindServiceImpl implements ISrLightconeMaterialBindService
{
    @Autowired
    private SrLightconeMaterialBindMapper srLightconeMaterialBindMapper;

    @Autowired
    private SrLightConesMapper srLightConesMapper;

    @Autowired
    private SrItemMapper srItemMapper;

    /**
     * 复用光锥统一保存逻辑（TRA/CAL 通过 seriesId 扩展高阶素材、晋升材料重新生成）；
     * 使用 @Lazy 避免潜在的循环依赖初始化问题
     */
    @Lazy
    @Autowired
    private ISrLightConesService srLightConesService;

    /** 透视列顺序：世界掉落、拟造花萼 */
    private static final String[] COLUMN_TYPES = {"TRA", "CAL"};
    private static final String[] COLUMN_DESCS = {"世界掉落", "拟造花萼"};

    /**
     * 查询光锥材料绑定
     * 
     * @param id 光锥材料绑定主键
     * @return 光锥材料绑定
     */
    @Override
    public SrLightconeMaterialBind selectSrLightconeMaterialBindById(Long id)
    {
        return srLightconeMaterialBindMapper.selectSrLightconeMaterialBindById(id);
    }

    /**
     * 查询光锥材料绑定列表
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 光锥材料绑定
     */
    @Override
    public List<SrLightconeMaterialBind> selectSrLightconeMaterialBindList(SrLightconeMaterialBind srLightconeMaterialBind)
    {
        return srLightconeMaterialBindMapper.selectSrLightconeMaterialBindList(srLightconeMaterialBind);
    }

    /**
     * 新增光锥材料绑定
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    @Override
    public int insertSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind)
    {
        srLightconeMaterialBind.setCreateTime(DateUtils.getNowDate());
        return srLightconeMaterialBindMapper.insertSrLightconeMaterialBind(srLightconeMaterialBind);
    }

    /**
     * 修改光锥材料绑定
     * 
     * @param srLightconeMaterialBind 光锥材料绑定
     * @return 结果
     */
    @Override
    public int updateSrLightconeMaterialBind(SrLightconeMaterialBind srLightconeMaterialBind)
    {
        srLightconeMaterialBind.setUpdateTime(DateUtils.getNowDate());
        return srLightconeMaterialBindMapper.updateSrLightconeMaterialBind(srLightconeMaterialBind);
    }

    /**
     * 批量删除光锥材料绑定
     * 
     * @param ids 需要删除的光锥材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrLightconeMaterialBindByIds(Long[] ids)
    {
        return srLightconeMaterialBindMapper.deleteSrLightconeMaterialBindByIds(ids);
    }

    /**
     * 删除光锥材料绑定信息
     * 
     * @param id 光锥材料绑定主键
     * @return 结果
     */
    @Override
    public int deleteSrLightconeMaterialBindById(Long id)
    {
        return srLightconeMaterialBindMapper.deleteSrLightconeMaterialBindById(id);
    }

    /**
     * 查询光锥材料绑定透视数据
     * 数据库中每个光锥存 TRA/CAL 各稀有度多行，导出时聚合成一行：
     * 每列取同类型中稀有度最低（二星基础素材）的材料名
     */
    @Override
    public List<SrLightconeMaterialBindExcel> selectBindExcelList(SrLightconeMaterialBind query)
    {
        List<SrLightconeMaterialBind> binds = srLightconeMaterialBindMapper.selectSrLightconeMaterialBindList(query);

        // 按光锥分组，保持列表原有顺序
        Map<Long, List<SrLightconeMaterialBind>> groups = new LinkedHashMap<>();
        for (SrLightconeMaterialBind bind : binds)
        {
            groups.computeIfAbsent(bind.getLightconeId(), k -> new ArrayList<>()).add(bind);
        }

        List<SrLightconeMaterialBindExcel> result = new ArrayList<>();
        for (List<SrLightconeMaterialBind> lightconeBinds : groups.values())
        {
            // 各类型代表行：取稀有度最低（二星基础素材）
            Map<String, SrLightconeMaterialBind> representative = new LinkedHashMap<>();
            for (SrLightconeMaterialBind bind : lightconeBinds)
            {
                String type = bind.getItemAscensionType();
                if (type == null)
                {
                    continue;
                }
                String key = type.toUpperCase();
                SrLightconeMaterialBind current = representative.get(key);
                if (current == null || isLowerRarity(bind, current))
                {
                    representative.put(key, bind);
                }
            }

            SrLightconeMaterialBindExcel excel = new SrLightconeMaterialBindExcel();
            excel.setLightconeName(lightconeBinds.get(0).getLightconeName());
            if (representative.containsKey("TRA"))
            {
                excel.setTraItemName(representative.get("TRA").getItemName());
            }
            if (representative.containsKey("CAL"))
            {
                excel.setCalItemName(representative.get("CAL").getItemName());
            }
            result.add(excel);
        }
        return result;
    }

    /**
     * candidate 稀有度低于 current（null 稀有度视为最大，不替换）
     */
    private boolean isLowerRarity(SrLightconeMaterialBind candidate, SrLightconeMaterialBind current)
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
     * 导入光锥材料绑定透视数据
     * 一行一个光锥，两列填材料名称（TRA/CAL 填二星基础素材名，按 seriesId 自动扩展高阶素材）。
     * 校验通过后复用 ISrLightConesService.saveAll 保存，与页面编辑走同一条链路。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BindImportResult importBindExcel(List<SrLightconeMaterialBindExcel> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            throw new RuntimeException("导入数据不能为空！");
        }

        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        List<String> failedLightcones = new ArrayList<>();
        int successNum = 0;
        int updateNum = 0;
        int skipNum = 0;

        for (SrLightconeMaterialBindExcel row : list)
        {
            String lightconeName = trimToEmpty(row.getLightconeName());
            if (StringUtils.isEmpty(lightconeName))
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、光锥名称不能为空");
                log.info("光锥材料绑定导入失败：光锥名称为空，行数据 {}", row);
                continue;
            }

            // 按名称精确查询光锥：不存在 / 同名多版本 都无法确定目标
            List<SrLightCones> lightcones = srLightConesMapper.selectSrLightConesListByName(lightconeName);
            if (lightcones.isEmpty())
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、光锥[").append(lightconeName).append("]不存在，请先导入光锥");
                failedLightcones.add(lightconeName);
                log.info("光锥材料绑定导入失败：光锥[{}]不存在，行数据 {}", lightconeName, row);
                continue;
            }
            if (lightcones.size() > 1)
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum)
                        .append("、光锥[").append(lightconeName).append("]存在多个实装版本记录，无法唯一确定");
                failedLightcones.add(lightconeName);
                log.info("光锥材料绑定导入失败：光锥[{}]存在多个实装版本记录，行数据 {}", lightconeName, row);
                continue;
            }
            SrLightCones lightcone = lightcones.get(0);

            // 解析两列材料（空单元格表示该类型不绑定）
            List<SrLightconeMaterialBind> materialBinds = new ArrayList<>();
            boolean rowValid = true;
            for (int i = 0; i < COLUMN_TYPES.length; i++)
            {
                String itemName = trimToEmpty(i == 0 ? row.getTraItemName() : row.getCalItemName());
                if (StringUtils.isEmpty(itemName))
                {
                    continue;
                }
                SrItem item = srItemMapper.selectSrItemByName(itemName);
                if (item == null)
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum)
                            .append("、光锥[").append(lightconeName).append("]")
                            .append(COLUMN_DESCS[i]).append("材料[").append(itemName).append("]不存在，请先导入材料");
                    failedLightcones.add(lightconeName);
                    log.info("光锥材料绑定导入失败：光锥[{}]{}材料[{}]不存在，行数据 {}",
                            lightconeName, COLUMN_DESCS[i], itemName, row);
                    rowValid = false;
                    break;
                }
                if (!COLUMN_TYPES[i].equalsIgnoreCase(item.getAscensionType()))
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum)
                            .append("、光锥[").append(lightconeName).append("]")
                            .append(COLUMN_DESCS[i]).append("列材料[").append(itemName).append("]类型不匹配");
                    failedLightcones.add(lightconeName);
                    log.info("光锥材料绑定导入失败：光锥[{}]{}列材料[{}]类型不匹配，行数据 {}",
                            lightconeName, COLUMN_DESCS[i], itemName, row);
                    rowValid = false;
                    break;
                }
                materialBinds.add(SrLightconeMaterialBind.builder()
                        .lightconeId(lightcone.getId())
                        .lightconeName(lightconeName)
                        .itemAscensionType(COLUMN_TYPES[i])
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
                        .append("、光锥[").append(lightconeName).append("]未填写任何材料");
                failedLightcones.add(lightconeName);
                log.info("光锥材料绑定导入失败：光锥[{}]未填写任何材料，行数据 {}", lightconeName, row);
                continue;
            }

            // 已存在绑定时按 updateSupport 决定跳过或覆盖
            List<SrLightconeMaterialBind> existing = srLightconeMaterialBindMapper.selectByLightConeId(lightcone.getId());
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

            // 复用统一保存：TRA/CAL 自动按 seriesId 扩展高阶素材并重新生成晋升材料
            LightConeSaveDTO dto = new LightConeSaveDTO();
            dto.setChangeType("full");
            dto.setLightCone(lightcone);
            dto.setMaterialBinds(materialBinds);
            srLightConesService.saveAll(dto);
        }

        // 构建返回消息
        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("导入完成：新增绑定 ").append(successNum).append(" 个光锥");
        if (updateNum > 0)
        {
            resultMsg.append("，更新 ").append(updateNum).append(" 个光锥");
        }
        if (skipNum > 0)
        {
            resultMsg.append("，跳过已存在 ").append(skipNum).append(" 个光锥");
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "，" + failureNum + " 条数据格式不正确：");
            resultMsg.append(failureMsg);
        }

        BindImportResult result = new BindImportResult();
        result.setMessage(resultMsg.toString());
        result.setFailedCharacters(failedLightcones);
        return result;
    }

    private String trimToEmpty(String value)
    {
        return value == null ? "" : value.trim();
    }
}
