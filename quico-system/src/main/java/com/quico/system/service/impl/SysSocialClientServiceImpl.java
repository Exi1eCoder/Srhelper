package com.quico.system.service.impl;

import java.util.List;
import com.quico.common.constant.UserConstants;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.quico.system.mapper.SysSocialClientMapper;
import com.quico.system.domain.SysSocialClient;
import com.quico.system.service.ISysSocialClientService;

/**
 * JustAuth三方登录配置Service业务层处理
 * 
 * @author quico
 * @date 2026-11-03
 */
@Service
public class SysSocialClientServiceImpl implements ISysSocialClientService 
{
    @Autowired
    private SysSocialClientMapper sysSocialClientMapper;

    /**
     * 查询JustAuth三方登录配置
     * 
     * @param clientId JustAuth三方登录配置主键
     * @return JustAuth三方登录配置
     */
    @Override
    public SysSocialClient selectSysSocialClientByClientId(Long clientId)
    {
        return sysSocialClientMapper.selectSysSocialClientByClientId(clientId);
    }

    /**
     * 按平台标识查询JustAuth三方登录配置
     * 
     * @param source 平台标识
     * @return JustAuth三方登录配置
     */
    @Override
    public SysSocialClient selectSysSocialClientBySource(String source)
    {
        return sysSocialClientMapper.selectSysSocialClientBySource(source);
    }

    /**
     * 查询JustAuth三方登录配置列表
     * 
     * @param sysSocialClient JustAuth三方登录配置
     * @return JustAuth三方登录配置
     */
    @Override
    public List<SysSocialClient> selectSysSocialClientList(SysSocialClient sysSocialClient)
    {
        return sysSocialClientMapper.selectSysSocialClientList(sysSocialClient);
    }

    /**
     * 校验平台标识是否唯一
     * 
     * @param sysSocialClient JustAuth三方登录配置
     * @return 结果
     */
    @Override
    public boolean checkSourceUnique(SysSocialClient sysSocialClient)
    {
        Long clientId = StringUtils.isNull(sysSocialClient.getClientId()) ? -1L : sysSocialClient.getClientId();
        SysSocialClient info = sysSocialClientMapper.checkSourceUnique(sysSocialClient.getSource());
        if (StringUtils.isNotNull(info) && info.getClientId().longValue() != clientId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增JustAuth三方登录配置
     * 
     * @param sysSocialClient JustAuth三方登录配置
     * @return 结果
     */
    @Override
    public int insertSysSocialClient(SysSocialClient sysSocialClient)
    {
        sysSocialClient.setCreateTime(DateUtils.getNowDate());
        return sysSocialClientMapper.insertSysSocialClient(sysSocialClient);
    }

    /**
     * 修改JustAuth三方登录配置
     * 
     * @param sysSocialClient JustAuth三方登录配置
     * @return 结果
     */
    @Override
    public int updateSysSocialClient(SysSocialClient sysSocialClient)
    {
        sysSocialClient.setUpdateTime(DateUtils.getNowDate());
        return sysSocialClientMapper.updateSysSocialClient(sysSocialClient);
    }

    /**
     * 批量删除JustAuth三方登录配置
     * 
     * @param clientIds 需要删除的JustAuth三方登录配置主键
     * @return 结果
     */
    @Override
    public int deleteSysSocialClientByClientIds(Long[] clientIds)
    {
        return sysSocialClientMapper.deleteSysSocialClientByClientIds(clientIds);
    }

    /**
     * 删除JustAuth三方登录配置信息
     * 
     * @param clientId JustAuth三方登录配置主键
     * @return 结果
     */
    @Override
    public int deleteSysSocialClientByClientId(Long clientId)
    {
        return sysSocialClientMapper.deleteSysSocialClientByClientId(clientId);
    }
}
