package com.quico.infra.convert.material;

import com.quico.common.pojo.PageResult;
import com.quico.infra.controller.admin.material.vo.MaterialCreateReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialExcelVO;
import com.quico.infra.controller.admin.material.vo.MaterialRespVO;
import com.quico.infra.controller.admin.material.vo.MaterialUpdateReqVO;
import com.quico.infra.dal.dataobject.material.MaterialDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 素材库 Convert
 *
 * @author quico
 */
@Mapper
public interface MaterialConvert {

    MaterialConvert INSTANCE = Mappers.getMapper(MaterialConvert.class);

    MaterialDO convert(MaterialCreateReqVO bean);

    MaterialDO convert(MaterialUpdateReqVO bean);

    MaterialRespVO convert(MaterialDO bean);

    List<MaterialRespVO> convertList(List<MaterialDO> list);

    PageResult<MaterialRespVO> convertPage(PageResult<MaterialDO> page);

    List<MaterialExcelVO> convertList02(List<MaterialDO> list);

}
