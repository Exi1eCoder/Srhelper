package com.quico.infra.mapper.materialgroup;

import com.quico.common.pojo.PageResult;
import com.quico.framework.mybatis.core.mapper.BaseMapperX;
import com.quico.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.quico.infra.controller.admin.materialgroup.vo.MaterialGroupExportReqVO;
import com.quico.infra.controller.admin.materialgroup.vo.MaterialGroupPageReqVO;
import com.quico.infra.dal.dataobject.materialgroup.MaterialGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 素材分组 Mapper
 *
 * @author quico
 */
@Mapper
public interface MaterialGroupMapper extends BaseMapperX<MaterialGroupDO> {

    default PageResult<MaterialGroupDO> selectPage(MaterialGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialGroupDO>()
                .betweenIfPresent(MaterialGroupDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(MaterialGroupDO::getName, reqVO.getName())
                .orderByDesc(MaterialGroupDO::getId));
    }

    default List<MaterialGroupDO> selectList(MaterialGroupExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MaterialGroupDO>()
                .betweenIfPresent(MaterialGroupDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(MaterialGroupDO::getName, reqVO.getName())
                .orderByDesc(MaterialGroupDO::getId));
    }

}
