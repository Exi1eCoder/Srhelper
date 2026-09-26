package com.quico.infra.service.impl;

import java.util.List;
import com.quico.common.core.domain.entity.SysUser;
import com.quico.common.utils.DateUtils;
import com.quico.infra.domain.vo.SrMaterialVO;
import com.quico.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.quico.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.quico.infra.domain.SrMaterialGroup;
import com.quico.infra.mapper.SrMaterialMapper;
import com.quico.infra.domain.SrMaterial;
import com.quico.infra.service.ISrMaterialService;

/**
 * 素材库Service业务层处理
 * 
 * @author quico
 * @date 2026-05-23
 */
@Service
public class SrMaterialServiceImpl implements ISrMaterialService 
{
    @Autowired
    private SrMaterialMapper srMaterialMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 查询素材库
     * 
     * @param id 素材库主键
     * @return 素材库
     */
    @Override
    public SrMaterial selectSrMaterialById(Long id)
    {
        return srMaterialMapper.selectSrMaterialById(id);
    }

    /**
     * 查询素材库列表
     * 
     * @param srMaterial 素材库
     * @return 素材库
     */
    @Override
    public List<SrMaterial> selectSrMaterialList(SrMaterial srMaterial)
    {
        return srMaterialMapper.selectSrMaterialList(srMaterial);
    }

    /**
     * 新增素材库
     * 
     * @param srMaterial 素材库
     * @return 结果
     */
    @Transactional
    @Override
    public int insertSrMaterial(SrMaterial srMaterial)
    {
        srMaterial.setCreateTime(DateUtils.getNowDate());
        int rows = srMaterialMapper.insertSrMaterial(srMaterial);
        insertSrMaterialGroup(srMaterial);
        return rows;
    }

    /**
     * 修改素材库
     * 
     * @param srMaterial 素材库
     * @return 结果
     */
    @Transactional
    @Override
    public int updateSrMaterial(SrMaterial srMaterial)
    {
        srMaterial.setUpdateTime(DateUtils.getNowDate());
        srMaterialMapper.deleteSrMaterialGroupById(srMaterial.getId());
        insertSrMaterialGroup(srMaterial);
        return srMaterialMapper.updateSrMaterial(srMaterial);
    }

    /**
     * 批量删除素材库
     * 
     * @param ids 需要删除的素材库主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteSrMaterialByIds(Long[] ids)
    {
        srMaterialMapper.deleteSrMaterialGroupByIds(ids);
        return srMaterialMapper.deleteSrMaterialByIds(ids);
    }

    /**
     * 删除素材库信息
     * 
     * @param id 素材库主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteSrMaterialById(Long id)
    {
        srMaterialMapper.deleteSrMaterialGroupById(id);
        return srMaterialMapper.deleteSrMaterialById(id);
    }

    /**
     * 新增素材分组信息
     *
     * @param srMaterial 素材库对象
     */
    public void insertSrMaterialGroup(SrMaterial srMaterial)
    {
        List<SrMaterialGroup> srMaterialGroupList = srMaterial.getSrMaterialGroupList();
        Long id = srMaterial.getId();
        if (StringUtils.isNotNull(srMaterialGroupList))
        {
            List<SrMaterialGroup> list = new ArrayList<SrMaterialGroup>();
            for (SrMaterialGroup srMaterialGroup : srMaterialGroupList)
            {
                srMaterialGroup.setId(id);
                list.add(srMaterialGroup);
            }
            if (list.size() > 0)
            {
                srMaterialMapper.batchSrMaterialGroup(list);
            }
        }
    }

    /**
     * 组装素材库VO列表（批量查用户和分组）
     *
     * @param materialList 素材库列表
     * @return 素材库VO列表
     */
    @Override
    public List<SrMaterialVO> assembleSrMaterialVOList(List<SrMaterial> materialList)
    {
        if (materialList == null || materialList.isEmpty())
        {
            return new ArrayList<>();
        }

        // 收集所有 creator ID 和 group ID
        Set<Long> creatorIds = materialList.stream()
                .map(SrMaterial::getCreator)
                .filter(StringUtils::isNotEmpty)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        Set<Long> groupIds = materialList.stream()
                .map(SrMaterial::getGroupId)
                .filter(StringUtils::isNotEmpty)
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        // 批量查询用户
        Map<Long, SysUser> userMap = Map.of();
        if (!creatorIds.isEmpty())
        {
            Long[] userIds = creatorIds.toArray(new Long[0]);
            List<SysUser> users = sysUserMapper.selectUserByIds(userIds);
            if (users != null)
            {
                userMap = users.stream()
                        .collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
            }
        }

        // 批量查询分组
        Map<Long, SrMaterialGroup> groupMap = Map.of();
        if (!groupIds.isEmpty())
        {
            Long[] gIds = groupIds.toArray(new Long[0]);
            List<SrMaterialGroup> groups = srMaterialMapper.selectSrMaterialGroupByIds(gIds);
            if (groups != null)
            {
                groupMap = groups.stream()
                        .collect(Collectors.toMap(SrMaterialGroup::getId, Function.identity()));
            }
        }

        // 组装 VO
        List<SrMaterialVO> voList = new ArrayList<>(materialList.size());
        for (SrMaterial material : materialList)
        {
            SrMaterialVO vo = new SrMaterialVO();
            vo.setId(material.getId());
            vo.setCreator(material.getCreator());
            vo.setType(material.getType());
            vo.setGroupId(material.getGroupId());
            vo.setName(material.getName());
            vo.setUrl(material.getUrl());
            vo.setUpdater(material.getUpdater());
            vo.setCreateTime(material.getCreateTime());
            vo.setUpdateTime(material.getUpdateTime());
            vo.setDeleted(material.getDeleted());
            vo.setTenantId(material.getTenantId());

            // 设置创建者名称
            if (StringUtils.isNotEmpty(material.getCreator()))
            {
                Long creatorId = Long.valueOf(material.getCreator());
                SysUser user = userMap.get(creatorId);
                if (user != null)
                {
                    vo.setCreatorName(user.getNickName());
                }
            }

            // 设置分组名称
            if (StringUtils.isNotEmpty(material.getGroupId()))
            {
                Long gId = Long.valueOf(material.getGroupId());
                SrMaterialGroup group = groupMap.get(gId);
                if (group != null)
                {
                    vo.setGroupName(group.getName());
                }
            }

            voList.add(vo);
        }

        return voList;
    }
}
