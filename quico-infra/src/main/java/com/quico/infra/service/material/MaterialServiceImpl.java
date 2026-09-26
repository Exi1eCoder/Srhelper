package com.quico.infra.service.material;

import com.quico.common.pojo.PageResult;
import com.quico.infra.controller.admin.material.vo.MaterialCreateReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialExportReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialPageReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialUpdateReqVO;
import com.quico.infra.convert.material.MaterialConvert;
import com.quico.infra.dal.dataobject.material.MaterialDO;
import com.quico.infra.mapper.material.MaterialMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static com.quico.common.exception.utils.ServiceExceptionUtil.exception;
import static com.quico.infra.enums.ErrorCodeConstants.MATERIAL_NOT_EXISTS;

/**
 * 素材库 Service 实现类
 *
 * @author quico
 */
@Service
@Validated
public class MaterialServiceImpl implements MaterialService {

    @Resource
    private MaterialMapper materialMapper;

    @Override
    public Long createMaterial(MaterialCreateReqVO createReqVO) {
        // 插入
        MaterialDO material = MaterialConvert.INSTANCE.convert(createReqVO);
        materialMapper.insert(material);
        // 返回
        return material.getId();
    }

    @Override
    public void updateMaterial(MaterialUpdateReqVO updateReqVO) {
        // 校验存在
        validateMaterialExists(updateReqVO.getId());
        // 更新
        MaterialDO updateObj = MaterialConvert.INSTANCE.convert(updateReqVO);
        materialMapper.updateById(updateObj);
    }

    @Override
    public void deleteMaterial(Long id) {
        // 校验存在
        validateMaterialExists(id);
        // 删除
        materialMapper.deleteById(id);
    }

    private void validateMaterialExists(Long id) {
        if (materialMapper.selectById(id) == null) {
            throw exception(MATERIAL_NOT_EXISTS);
        }
    }

    @Override
    public MaterialDO getMaterial(Long id) {
        return materialMapper.selectById(id);
    }

    @Override
    public List<MaterialDO> getMaterialList(Collection<Long> ids) {
        return materialMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MaterialDO> getMaterialPage(MaterialPageReqVO pageReqVO) {
        return materialMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MaterialDO> getMaterialList(MaterialExportReqVO exportReqVO) {
        return materialMapper.selectList(exportReqVO);
    }

}
