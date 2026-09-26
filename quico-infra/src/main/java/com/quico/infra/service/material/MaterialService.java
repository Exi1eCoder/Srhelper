package com.quico.infra.service.material;

import com.quico.common.pojo.PageResult;
import com.quico.infra.controller.admin.material.vo.MaterialCreateReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialExportReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialPageReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialUpdateReqVO;
import com.quico.infra.dal.dataobject.material.MaterialDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 素材库 Service 接口
 *
 * @author quico
 */
public interface MaterialService {

    /**
     * 创建素材库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaterial(@Valid MaterialCreateReqVO createReqVO);

    /**
     * 更新素材库
     *
     * @param updateReqVO 更新信息
     */
    void updateMaterial(@Valid MaterialUpdateReqVO updateReqVO);

    /**
     * 删除素材库
     *
     * @param id 编号
     */
    void deleteMaterial(Long id);

    /**
     * 获得素材库
     *
     * @param id 编号
     * @return 素材库
     */
    MaterialDO getMaterial(Long id);

    /**
     * 获得素材库列表
     *
     * @param ids 编号
     * @return 素材库列表
     */
    List<MaterialDO> getMaterialList(Collection<Long> ids);

    /**
     * 获得素材库分页
     *
     * @param pageReqVO 分页查询
     * @return 素材库分页
     */
    PageResult<MaterialDO> getMaterialPage(MaterialPageReqVO pageReqVO);

    /**
     * 获得素材库列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 素材库列表
     */
    List<MaterialDO> getMaterialList(MaterialExportReqVO exportReqVO);

}
