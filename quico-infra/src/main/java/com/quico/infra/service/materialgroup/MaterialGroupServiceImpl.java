package com.quico.infra.service.materialgroup;

import com.quico.common.pojo.PageResult;
import com.quico.infra.controller.admin.materialgroup.vo.MaterialGroupCreateReqVO;
import com.quico.infra.controller.admin.materialgroup.vo.MaterialGroupExportReqVO;
import com.quico.infra.controller.admin.materialgroup.vo.MaterialGroupPageReqVO;
import com.quico.infra.controller.admin.materialgroup.vo.MaterialGroupUpdateReqVO;
import com.quico.infra.convert.materialgroup.MaterialGroupConvert;
import com.quico.infra.dal.dataobject.materialgroup.MaterialGroupDO;
import com.quico.infra.mapper.materialgroup.MaterialGroupMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static com.quico.common.exception.utils.ServiceExceptionUtil.exception;
import static com.quico.infra.enums.ErrorCodeConstants.*;

/**
 * 素材分组 Service 实现类
 *
 * @author quico
 */
@Service
@Validated
public class MaterialGroupServiceImpl implements MaterialGroupService {

    @Resource
    private MaterialGroupMapper materialGroupMapper;

    @Override
    public Long createMaterialGroup(MaterialGroupCreateReqVO createReqVO) {
        // 插入
        MaterialGroupDO materialGroup = MaterialGroupConvert.INSTANCE.convert(createReqVO);
        materialGroupMapper.insert(materialGroup);
        // 返回
        return materialGroup.getId();
    }

    @Override
    public void updateMaterialGroup(MaterialGroupUpdateReqVO updateReqVO) {
        // 校验存在
        validateMaterialGroupExists(updateReqVO.getId());
        // 更新
        MaterialGroupDO updateObj = MaterialGroupConvert.INSTANCE.convert(updateReqVO);
        materialGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteMaterialGroup(Long id) {
        // 校验存在
        validateMaterialGroupExists(id);
        // 删除
        materialGroupMapper.deleteById(id);
    }

    private void validateMaterialGroupExists(Long id) {
        if (materialGroupMapper.selectById(id) == null) {
            throw exception(MATERIAL_GROUP_NOT_EXISTS);
        }
    }

    @Override
    public MaterialGroupDO getMaterialGroup(Long id) {
        return materialGroupMapper.selectById(id);
    }

    @Override
    public List<MaterialGroupDO> getMaterialGroupList(Collection<Long> ids) {
        return materialGroupMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MaterialGroupDO> getMaterialGroupPage(MaterialGroupPageReqVO pageReqVO) {
        return materialGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MaterialGroupDO> getMaterialGroupList(MaterialGroupExportReqVO exportReqVO) {
        return materialGroupMapper.selectList(exportReqVO);
    }

}
