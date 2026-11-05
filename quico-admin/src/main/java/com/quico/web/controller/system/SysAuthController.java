package com.quico.web.controller.system;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import com.quico.common.constant.Constants;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.core.domain.entity.SysUser;
import com.quico.common.core.domain.model.LoginUser;
import com.quico.common.enums.UserStatus;
import com.quico.common.exception.ServiceException;
import com.quico.common.utils.SecurityUtils;
import com.quico.common.utils.StringUtils;
import com.quico.framework.utils.AuthUtils;
import com.quico.framework.web.service.SysPermissionService;
import com.quico.framework.web.service.TokenService;
import com.quico.system.domain.SysAuthUser;
import com.quico.system.domain.SysSocialClient;
import com.quico.system.mapper.SysUserMapper;
import com.quico.system.service.ISysSocialClientService;
import com.quico.system.service.ISysUserService;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;

/**
 * 第三方认证授权处理
 * 
 * @author quico
 */
@Slf4j
@RestController
@RequestMapping("/system/auth")
public class SysAuthController extends BaseController
{
    private final AuthStateCache authStateCache = AuthDefaultStateCache.INSTANCE;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private ISysSocialClientService socialClientService;

    /**
     * 按平台标识查询三方登录配置
     * 
     * @param source 平台标识
     * @return 三方登录配置
     */
    private SysSocialClient getSocialClient(String source)
    {
        SysSocialClient socialClient = socialClientService.selectSysSocialClientBySource(source);
        if (StringUtils.isNotNull(socialClient))
        {
            log.debug("三方登录配置查询成功，source={}, clientId={}, clientName={}, appId={}, redirectUri={}, agentId={}, status={}",
                    socialClient.getSource(), socialClient.getClientId(), socialClient.getClientName(),
                    socialClient.getAppId(), socialClient.getRedirectUri(), socialClient.getAgentId(),
                    socialClient.getStatus());
        }
        else
        {
            log.debug("未查询到三方登录配置，source={}", source);
        }
        return socialClient;
    }

    /**
     * 认证授权
     * 
     * @param source
     * @throws IOException
     */
    @GetMapping("/binding/{source}")
    @ResponseBody
    public AjaxResult authBinding(@PathVariable("source") String source, HttpServletRequest request) throws IOException
    {
        LoginUser tokenUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(tokenUser) && userMapper.checkAuthUser(tokenUser.getUserId(), source) > 0)
        {
            return error(source + "平台账号已经绑定");
        }

        SysSocialClient socialClient = getSocialClient(source);
        if (StringUtils.isNull(socialClient))
        {
            return error(source + "平台账号暂不支持");
        }
        if (UserStatus.DISABLE.getCode().equals(socialClient.getStatus()))
        {
            return error(source + "平台账号已停用");
        }
        AuthRequest authRequest = AuthUtils.getAuthRequest(source, socialClient.getAppId(),
                socialClient.getAppSecret(), socialClient.getRedirectUri(), authStateCache);
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        return success(authorizeUrl);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/social-login/{source}")
    public AjaxResult socialLogin(@PathVariable("source") String source, AuthCallback callback, HttpServletRequest request)
    {
        SysSocialClient socialClient = getSocialClient(source);
        if (StringUtils.isNull(socialClient))
        {
            return AjaxResult.error(10002, "第三方平台系统不支持或未提供来源");
        }
        if (UserStatus.DISABLE.getCode().equals(socialClient.getStatus()))
        {
            return AjaxResult.error(10002, "第三方平台系统已停用");
        }
        AuthRequest authRequest = AuthUtils.getAuthRequest(source, socialClient.getAppId(),
                socialClient.getAppSecret(), socialClient.getRedirectUri(), authStateCache);
        AuthResponse<AuthUser> response = authRequest.login(callback);
        if (response.ok())
        {
            LoginUser tokenUser = tokenService.getLoginUser(request);
            if (StringUtils.isNotNull(tokenUser))
            {
                SysUser user = userMapper.selectAuthUserByUuid(source + response.getData().getUuid());
                if (StringUtils.isNotNull(user))
                {
                    String token = tokenService.createToken(SecurityUtils.getLoginUser());
                    return success().put(Constants.TOKEN, token);
                }
                // 若已经登录则直接绑定系统账号
                SysAuthUser authUser = new SysAuthUser();
                authUser.setAvatar(response.getData().getAvatar());
                authUser.setUuid(source + response.getData().getUuid());
                authUser.setUserId(SecurityUtils.getUserId());
                authUser.setUserName(response.getData().getUsername());
                authUser.setNickName(response.getData().getNickname());
                authUser.setEmail(response.getData().getEmail());
                authUser.setSource(source);
                userMapper.insertAuthUser(authUser);
                String token = tokenService.createToken(SecurityUtils.getLoginUser());
                return success().put(Constants.TOKEN, token);
            }
            SysUser authUser = userMapper.selectAuthUserByUuid(source + response.getData().getUuid());
            if (StringUtils.isNotNull(authUser))
            {
                SysUser user = userService.selectUserByUserName(authUser.getUserName());
                if (StringUtils.isNull(user))
                {
                    throw new ServiceException("登录用户：" + user.getUserName() + " 不存在");
                }
                else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
                {
                    throw new ServiceException("对不起，您的账号：" + user.getUserName() + " 已被删除");
                }
                else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
                {
                    throw new ServiceException("对不起，您的账号：" + user.getUserName() + " 已停用");
                }
                LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
                String token = tokenService.createToken(loginUser);
                return success().put(Constants.TOKEN, token);
            }
            else
            {
                return AjaxResult.error(10002, "对不起，您没有绑定注册用户，请先注册后在个人中心绑定第三方授权信息！");
            }
        }
        return AjaxResult.error(10002, "对不起，授权信息验证不通过，请联系管理员");
    }

    /**
     * 取消授权
     */
    @DeleteMapping(value = "/unlock/{authId}")
    public AjaxResult unlockAuth(@PathVariable Long authId)
    {
        return toAjax(userMapper.deleteAuthUser(authId));
    }
}
