package com.quico.infra.dal.mysql.db;

import com.quico.framework.mybatis.core.mapper.BaseMapperX;
import com.quico.infra.dal.dataobject.db.DataSourceConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置 Mapper
 *
 * @author yshop
 */
@Mapper
public interface DataSourceConfigMapper extends BaseMapperX<DataSourceConfigDO> {
}
