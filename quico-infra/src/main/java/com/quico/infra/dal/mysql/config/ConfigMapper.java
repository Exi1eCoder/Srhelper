package com.quico.infra.dal.mysql.config;

import com.quico.common.pojo.PageResult;
import com.quico.framework.mybatis.core.mapper.BaseMapperX;
import com.quico.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.quico.infra.controller.admin.config.vo.ConfigPageReqVO;
import com.quico.infra.dal.dataobject.config.ConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigMapper extends BaseMapperX<ConfigDO> {

    default ConfigDO selectByKey(String key) {
        return selectOne(ConfigDO::getConfigKey, key);
    }

    default PageResult<ConfigDO> selectPage(ConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ConfigDO>()
                .likeIfPresent(ConfigDO::getName, reqVO.getName())
                .likeIfPresent(ConfigDO::getConfigKey, reqVO.getKey())
                .eqIfPresent(ConfigDO::getType, reqVO.getType())
                .betweenIfPresent(ConfigDO::getCreateTime, reqVO.getCreateTime()));
    }

}
