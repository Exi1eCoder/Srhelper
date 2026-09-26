package com.quico.infra.mapper.material;

import com.quico.common.pojo.PageResult;
import com.quico.framework.mybatis.core.mapper.BaseMapperX;
import com.quico.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.quico.infra.controller.admin.material.vo.MaterialExportReqVO;
import com.quico.infra.controller.admin.material.vo.MaterialPageReqVO;
import com.quico.infra.dal.dataobject.material.MaterialDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 素材库 Mapper
 *
 * @author quico
 */
@Mapper
public interface MaterialMapper extends BaseMapperX<MaterialDO> {

    default PageResult<MaterialDO> selectPage(MaterialPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialDO>()
                .betweenIfPresent(MaterialDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(MaterialDO::getType, reqVO.getType())
                .eqIfPresent(MaterialDO::getGroupId, reqVO.getGroupId())
                .likeIfPresent(MaterialDO::getName, reqVO.getName())
                .eqIfPresent(MaterialDO::getUrl, reqVO.getUrl())
                .orderByDesc(MaterialDO::getId));
    }

    default List<MaterialDO> selectList(MaterialExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MaterialDO>()
                .betweenIfPresent(MaterialDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(MaterialDO::getType, reqVO.getType())
                .eqIfPresent(MaterialDO::getGroupId, reqVO.getGroupId())
                .likeIfPresent(MaterialDO::getName, reqVO.getName())
                .eqIfPresent(MaterialDO::getUrl, reqVO.getUrl())
                .orderByDesc(MaterialDO::getId));
    }

}
