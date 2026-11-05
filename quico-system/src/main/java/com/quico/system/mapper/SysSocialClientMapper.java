package com.quico.system.mapper;

import java.util.List;
import com.quico.system.domain.SysSocialClient;

/**
 * JustAuth三方登录配置Mapper接口
 * 
 * @author quico
 * @date 2026-11-03
 */
public interface SysSocialClientMapper 
{
    /**
     * 查询JustAuth三方登录配置
     * 
     * @param clientId JustAuth三方登录配置主键
     * @return JustAuth三方登录配置
     */
    public SysSocialClient selectSysSocialClientByClientId(Long clientId);

    /**
     * 按平台标识查询JustAuth三方登录配置
     * 
     * @param source 平台标识
     * @return JustAuth三方登录配置
     */
    public SysSocialClient selectSysSocialClientBySource(String source);

    /**
     * 查询JustAuth三方登录配置列表
     * 
     * @param sysSocialClient JustAuth三方登录配置
     * @return JustAuth三方登录配置集合
     */
    public List<SysSocialClient> selectSysSocialClientList(SysSocialClient sysSocialClient);

    /**
     * 校验平台标识是否唯一
     * 
     * @param source 平台标识
     * @return JustAuth三方登录配置
     */
    public SysSocialClient checkSourceUnique(String source);

    /**
     * 新增JustAuth三方登录配置
     * 
     * @param sysSocialClient JustAuth三方登录配置
     * @return 结果
     */
    public int insertSysSocialClient(SysSocialClient sysSocialClient);

    /**
     * 修改JustAuth三方登录配置
     * 
     * @param sysSocialClient JustAuth三方登录配置
     * @return 结果
     */
    public int updateSysSocialClient(SysSocialClient sysSocialClient);

    /**
     * 删除JustAuth三方登录配置
     * 
     * @param clientId JustAuth三方登录配置主键
     * @return 结果
     */
    public int deleteSysSocialClientByClientId(Long clientId);

    /**
     * 批量删除JustAuth三方登录配置
     * 
     * @param clientIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysSocialClientByClientIds(Long[] clientIds);
}
