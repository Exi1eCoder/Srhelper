package com.quico.framework.web.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.quico.common.constant.CacheConstants;
import com.quico.common.constant.Constants;
import com.quico.common.core.domain.entity.SysRole;
import com.quico.common.core.domain.entity.SysUser;
import com.quico.common.core.domain.model.LoginUser;
import com.quico.common.core.redis.RedisCache;
import com.quico.common.enums.UserStatus;
import com.quico.common.exception.ServiceException;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.SecurityUtils;
import com.quico.common.utils.StringUtils;
import com.quico.common.utils.ip.IpUtils;
import com.quico.common.utils.uuid.IdUtils;
import com.quico.framework.manager.AsyncManager;
import com.quico.framework.manager.factory.AsyncFactory;
import com.quico.system.domain.SysUserWechat;
import com.quico.system.mapper.SysRoleMapper;
import com.quico.system.mapper.SysUserWechatMapper;
import com.quico.system.service.ISysUserService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * 微信小程序登录服务
 *
 * @author quico
 */
@Component
public class WechatMiniLoginService
{
    private static final Logger log = LoggerFactory.getLogger(WechatMiniLoginService.class);

    /** 登录日志中无法识别用户时使用的账号标识，同时用于区分微信登录类型 */
    private static final String LOGIN_NAME_UNKNOWN = "wechat_mini";

    private static final String MSG_CODE_INVALID = "登录凭证已失效，请重新进入小程序";

    private static final String MSG_WX_BUSY = "微信服务繁忙，请稍后重试";

    private static final String MSG_WX_CONFIG_ERROR = "微信登录配置有误，请联系管理员";

    private static final String MSG_NOT_CONFIGURED = "微信登录未配置，请联系管理员";

    private static final String MSG_DISABLED = "该账号已被停用，请联系管理员";

    private static final String MSG_DELETED = "该账号已被删除，请联系管理员";

    private static final String MSG_USER_NOT_FOUND = "微信账号不存在，请联系管理员";

    private static final String MSG_BIND_REQUIRED = "首次登录请先绑定手机号";

    private static final String MSG_CREATE_FAILED = "微信账号创建失败，请稍后重试";

    private static final String MSG_SYSTEM_ERROR = "登录失败，请稍后重试";

    private static final String MSG_RATE_LIMIT = "操作过于频繁，请稍后再试";

    /** 同一 IP + openId 每分钟最多登录次数 */
    private static final int RATE_LIMIT_COUNT = 10;

    /** 限流时间窗口（秒） */
    private static final int RATE_LIMIT_SECONDS = 60;

    /** 无效/已使用的登录凭证错误码 */
    private static final int[] INVALID_CODE_ERROR_CODES = { 40029, 40163, 41008 };

    /** AppId 相关错误码 */
    private static final int[] CONFIG_ERROR_CODES = { 40013, 41002 };

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserWechatMapper userWechatMapper;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisCache redisCache;

    /** 小程序AppId */
    @Value("${wechat.miniapp.appid:}")
    private String appid;

    /** 小程序AppSecret */
    @Value("${wechat.miniapp.secret:}")
    private String secret;

    /** 是否自动创建微信用户 */
    @Value("${wechat.miniapp.auto-register:true}")
    private boolean autoRegister;

    /** 自动创建用户的默认部门，0 表示不分配部门 */
    @Value("${wechat.miniapp.default-dept-id:0}")
    private Long defaultDeptId;

    /** 自动创建用户的默认角色 */
    @Value("${wechat.miniapp.default-role-key:common}")
    private String defaultRoleKey;

    /**
     * 微信小程序一键登录
     *
     * @param code wx.login 返回的临时登录凭证
     * @return 令牌
     */
    @Transactional(rollbackFor = Exception.class)
    public String login(String code)
    {
        if (StringUtils.isEmpty(appid) || StringUtils.isEmpty(secret))
        {
            recordLoginFail(LOGIN_NAME_UNKNOWN, MSG_NOT_CONFIGURED);
            throw new ServiceException(MSG_NOT_CONFIGURED);
        }
        if (StringUtils.isEmpty(code))
        {
            recordLoginFail(LOGIN_NAME_UNKNOWN, MSG_CODE_INVALID);
            throw new ServiceException(MSG_CODE_INVALID);
        }

        WxMaJscode2SessionResult session = getSessionInfo(code);
        String openid = session.getOpenid();
        if (StringUtils.isEmpty(openid))
        {
            recordLoginFail(LOGIN_NAME_UNKNOWN, MSG_WX_BUSY);
            throw new ServiceException(MSG_WX_BUSY);
        }
        String unionid = session.getUnionid();

        String loginName = LOGIN_NAME_UNKNOWN;
        try
        {
            checkRateLimit(openid);
            SysUser user = resolveUser(openid, unionid);
            loginName = user.getUserName();
            userService.updateLoginInfo(user.getUserId(), IpUtils.getIpAddr(), DateUtils.getNowDate());
            Set<String> permissions = permissionService.getMenuPermission(user);
            LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissions);
            String token = tokenService.createToken(loginUser);
            recordLoginSuccess(loginName);
            return token;
        }
        catch (ServiceException e)
        {
            recordLoginFail(loginName, e.getMessage());
            throw e;
        }
        catch (Exception e)
        {
            log.error("微信小程序登录异常", e);
            recordLoginFail(loginName, MSG_SYSTEM_ERROR);
            throw new ServiceException(MSG_SYSTEM_ERROR);
        }
    }

    /**
     * 换取微信会话信息
     *
     * @param code 临时登录凭证
     * @return 会话信息（sessionKey 不落日志、不缓存、不返回前端）
     */
    private WxMaJscode2SessionResult getSessionInfo(String code)
    {
        try
        {
            return wxMaService.getUserService().getSessionInfo(code);
        }
        catch (WxErrorException e)
        {
            int errCode = e.getError() == null ? 0 : e.getError().getErrorCode();
            // 仅记录错误码，禁止记录 code、sessionKey、secret
            log.info("微信小程序换取会话失败，errCode={}", errCode);
            String message = resolveWxErrorMessage(errCode);
            recordLoginFail(LOGIN_NAME_UNKNOWN, message);
            throw new ServiceException(message);
        }
        catch (Exception e)
        {
            log.warn("微信小程序换取会话异常，type={}", e.getClass().getSimpleName());
            recordLoginFail(LOGIN_NAME_UNKNOWN, MSG_WX_BUSY);
            throw new ServiceException(MSG_WX_BUSY);
        }
    }

    /**
     * 解析微信错误码对应的用户提示
     *
     * @param errCode 微信错误码
     * @return 中文提示
     */
    private String resolveWxErrorMessage(int errCode)
    {
        if (contains(INVALID_CODE_ERROR_CODES, errCode))
        {
            return MSG_CODE_INVALID;
        }
        if (contains(CONFIG_ERROR_CODES, errCode))
        {
            return MSG_WX_CONFIG_ERROR;
        }
        return MSG_WX_BUSY;
    }

    private boolean contains(int[] codes, int code)
    {
        for (int item : codes)
        {
            if (item == code)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * IP + openId 维度限流
     *
     * @param openid 微信openId
     */
    private void checkRateLimit(String openid)
    {
        String key = CacheConstants.RATE_LIMIT_KEY + "wechat_mini:" + IpUtils.getIpAddr() + ":" + openid;
        Object value = redisCache.redisTemplate.opsForValue().increment(key);
        Long count = value == null ? null : Long.valueOf(value.toString());
        if (count != null && count.longValue() == 1L)
        {
            redisCache.expire(key, RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        }
        if (count != null && count.longValue() > RATE_LIMIT_COUNT)
        {
            throw new ServiceException(MSG_RATE_LIMIT);
        }
    }

    /**
     * 查询或创建微信绑定的系统用户
     *
     * @param openid 微信openId
     * @param unionid 微信unionId
     * @return 系统用户
     */
    private SysUser resolveUser(String openid, String unionid)
    {
        SysUserWechat binding = findBinding(openid, unionid);
        if (binding != null)
        {
            SysUser user = userService.selectUserById(binding.getUserId());
            if (user == null)
            {
                throw new ServiceException(MSG_USER_NOT_FOUND);
            }
            validateUser(user);
            if (StringUtils.isNotEmpty(unionid) && StringUtils.isEmpty(binding.getUnionid()))
            {
                userWechatMapper.updateUnionid(binding.getId(), unionid);
            }
            return user;
        }

        if (!autoRegister)
        {
            throw new ServiceException(MSG_BIND_REQUIRED, 600);
        }

        SysUser user = createWechatUser(openid);
        bindWechatUser(user.getUserId(), openid, unionid);
        return user;
    }

    /**
     * 优先按 unionId 查询绑定关系，其次按 appid + openId 查询
     *
     * @param openid 微信openId
     * @param unionid 微信unionId
     * @return 绑定关系
     */
    private SysUserWechat findBinding(String openid, String unionid)
    {
        if (StringUtils.isNotEmpty(unionid))
        {
            SysUserWechat binding = userWechatMapper.selectByUnionid(unionid);
            if (binding != null)
            {
                return binding;
            }
        }
        return userWechatMapper.selectByAppidAndOpenid(appid, openid);
    }

    /**
     * 校验用户状态
     *
     * @param user 系统用户
     */
    private void validateUser(SysUser user)
    {
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            throw new ServiceException(MSG_DELETED);
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            throw new ServiceException(MSG_DISABLED);
        }
    }

    /**
     * 自动创建微信用户
     *
     * @param openid 微信openId
     * @return 创建后的系统用户
     */
    private SysUser createWechatUser(String openid)
    {
        SysUser user = new SysUser();
        user.setUserName(generateUserName(openid));
        user.setNickName(generateNickName(openid));
        // 默认不分配部门，仅分配角色；如需分配，将 default-dept-id 配置为对应部门ID
        if (defaultDeptId != null && defaultDeptId > 0)
        {
            user.setDeptId(defaultDeptId);
        }
        // 随机密码且不可通过账号密码登录
        user.setPassword(SecurityUtils.encryptPassword(IdUtils.fastUUID()));
        user.setStatus(UserStatus.OK.getCode());
        user.setCreateBy("wechat");
        user.setRemark("微信小程序自动注册");
        user.setRoleIds(new Long[] { resolveDefaultRoleId() });
        userService.insertUser(user);
        if (user.getUserId() == null)
        {
            throw new ServiceException(MSG_CREATE_FAILED);
        }
        SysUser created = userService.selectUserById(user.getUserId());
        if (created == null)
        {
            throw new ServiceException(MSG_CREATE_FAILED);
        }
        return created;
    }

    /**
     * 新增微信绑定关系
     *
     * @param userId 系统用户ID
     * @param openid 微信openId
     * @param unionid 微信unionId
     */
    private void bindWechatUser(Long userId, String openid, String unionid)
    {
        SysUserWechat binding = new SysUserWechat();
        binding.setUserId(userId);
        binding.setAppid(appid);
        binding.setOpenid(openid);
        binding.setUnionid(unionid);
        userWechatMapper.insertUserWechat(binding);
    }

    /**
     * 生成唯一登录账号，格式 wx_ + openId 后 8 位，冲突时追加随机后缀
     *
     * @param openid 微信openId
     * @return 用户账号
     */
    private String generateUserName(String openid)
    {
        String suffix = StringUtils.substring(openid, Math.max(0, openid.length() - 8));
        String baseName = "wx_" + suffix;
        if (isUserNameUnique(baseName))
        {
            return baseName;
        }
        for (int i = 0; i < 5; i++)
        {
            String candidate = baseName + "_" + StringUtils.substring(IdUtils.simpleUUID(), 0, 4);
            if (isUserNameUnique(candidate))
            {
                return candidate;
            }
        }
        throw new ServiceException(MSG_CREATE_FAILED);
    }

    private boolean isUserNameUnique(String userName)
    {
        SysUser probe = new SysUser();
        probe.setUserName(userName);
        return userService.checkUserNameUnique(probe);
    }

    /**
     * 生成默认昵称
     *
     * @param openid 微信openId
     * @return 用户昵称
     */
    private String generateNickName(String openid)
    {
        return "微信用户" + StringUtils.substring(openid, Math.max(0, openid.length() - 4));
    }

    /**
     * 解析默认角色ID
     *
     * @return 角色ID
     */
    private Long resolveDefaultRoleId()
    {
        SysRole role = roleMapper.checkRoleKeyUnique(defaultRoleKey);
        if (role == null || role.getRoleId() == null)
        {
            throw new ServiceException("微信登录默认角色未配置，请联系管理员");
        }
        return role.getRoleId();
    }

    private void recordLoginSuccess(String userName)
    {
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGIN_SUCCESS, "微信小程序登录成功"));
    }

    private void recordLoginFail(String userName, String message)
    {
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGIN_FAIL, "微信小程序登录失败：" + message));
    }
}
