package com.quico.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.quico.system.domain.SysUserWechat;

/**
 * 微信小程序用户绑定 数据层
 *
 * @author quico
 */
public interface SysUserWechatMapper
{
    /**
     * 根据小程序AppId和openId查询绑定关系
     *
     * @param appid 小程序AppId
     * @param openid 微信openId
     * @return 绑定关系
     */
    public SysUserWechat selectByAppidAndOpenid(@Param("appid") String appid, @Param("openid") String openid);

    /**
     * 根据unionId查询绑定关系
     *
     * @param unionid 微信unionId
     * @return 绑定关系
     */
    public SysUserWechat selectByUnionid(@Param("unionid") String unionid);

    /**
     * 新增微信绑定关系
     *
     * @param userWechat 绑定关系
     * @return 结果
     */
    public int insertUserWechat(SysUserWechat userWechat);

    /**
     * 补充绑定关系中的unionId
     *
     * @param id 主键
     * @param unionid 微信unionId
     * @return 结果
     */
    public int updateUnionid(@Param("id") Long id, @Param("unionid") String unionid);
}
