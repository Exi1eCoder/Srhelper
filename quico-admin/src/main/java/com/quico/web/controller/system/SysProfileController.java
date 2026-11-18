package com.quico.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.quico.common.annotation.Log;
import com.quico.common.config.QuicoConfig;
import com.quico.common.core.controller.BaseController;
import com.quico.common.core.domain.AjaxResult;
import com.quico.common.core.domain.entity.SysUser;
import com.quico.common.core.domain.model.LoginUser;
import com.quico.common.enums.BusinessType;
import com.quico.common.utils.DateUtils;
import com.quico.common.utils.SecurityUtils;
import com.quico.common.utils.StringUtils;
import com.quico.common.utils.file.FileUploadUtils;
import com.quico.common.utils.file.FileUtils;
import com.quico.common.utils.file.MimeTypeUtils;
import com.quico.framework.web.service.EmailVerifyCodeService;
import com.quico.framework.web.service.SmsVerifyCodeService;
import com.quico.framework.web.service.TokenService;
import com.quico.system.domain.dto.ProfileUpdateBody;
import com.quico.system.service.ISysUserService;
import com.quico.common.annotation.RepeatSubmit;

/**
 * 个人信息 业务处理
 * 
 * @author quico
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailVerifyCodeService emailVerifyCodeService;

    @Autowired
    private SmsVerifyCodeService smsVerifyCodeService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile()
    {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("auths", userService.selectAuthUserListByUserId(user.getUserId()));
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
        return ajax;
    }

    /**
     * 发送邮箱验证码
     *
     * 验证码发送到「待绑定的新邮箱」，用户拿到验证码即证明可控制该邮箱
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/emailCode")
    public AjaxResult emailCode(@RequestBody Map<String, String> params)
    {
        LoginUser loginUser = getLoginUser();
        String email = StringUtils.trim(params.get("email"));
        if (StringUtils.isNotEmpty(email))
        {
            SysUser probe = new SysUser();
            probe.setUserId(loginUser.getUserId());
            probe.setEmail(email);
            if (!userService.checkEmailUnique(probe))
            {
                return error("该邮箱已被其他账号绑定");
            }
        }
        emailVerifyCodeService.sendCode(email, loginUser.getUserId());
        return success("验证码已发送，请查收邮箱");
    }

    /**
     * 发送手机验证码
     *
     * 验证码发送到「待绑定的新手机号」，用户拿到验证码即证明可控制该号码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/phoneCode")
    public AjaxResult phoneCode(@RequestBody Map<String, String> params)
    {
        LoginUser loginUser = getLoginUser();
        String phonenumber = StringUtils.trim(params.get("phonenumber"));
        if (StringUtils.isNotEmpty(phonenumber))
        {
            SysUser probe = new SysUser();
            probe.setUserId(loginUser.getUserId());
            probe.setPhonenumber(phonenumber);
            if (!userService.checkPhoneUnique(probe))
            {
                return error("该手机号已被其他账号绑定");
            }
        }
        smsVerifyCodeService.sendCode(phonenumber, loginUser.getUserId());
        return success("验证码已发送，请查收短信");
    }

    /**
     * 修改用户
     *
     * 邮箱、手机号发生变更时必须先完成对应验证码校验
     */
    @RepeatSubmit(interval = 3000, message = "请求过于频繁，请稍后再试")
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody ProfileUpdateBody body)
    {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getUser();
        String newEmail = StringUtils.trim(body.getEmail());
        if (StringUtils.isNotEmpty(newEmail) && !newEmail.equals(StringUtils.trim(currentUser.getEmail())))
        {
            // 先校验验证码再修改缓存中的用户对象，避免校验失败污染登录态
            emailVerifyCodeService.verify(newEmail, loginUser.getUserId(), body.getCode());
        }
        String newPhonenumber = StringUtils.trim(body.getPhonenumber());
        if (StringUtils.isNotEmpty(newPhonenumber)
                && !newPhonenumber.equals(StringUtils.trim(currentUser.getPhonenumber())))
        {
            smsVerifyCodeService.verify(newPhonenumber, loginUser.getUserId(), body.getPhoneCode());
        }
        currentUser.setNickName(body.getNickName());
        currentUser.setEmail(newEmail);
        currentUser.setPhonenumber(newPhonenumber);
        currentUser.setSex(body.getSex());
        if (StringUtils.isNotEmpty(currentUser.getPhonenumber()) && !userService.checkPhoneUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(currentUser.getEmail()) && !userService.checkEmailUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(currentUser) > 0)
        {
            // 更新缓存用户信息
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params)
    {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginUser loginUser = getLoginUser();
        Long userId = loginUser.getUserId();
        SysUser user = userService.selectUserById(userId);
        String password = user.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userId, newPassword) > 0)
        {
            // 更新缓存用户密码&密码最后更新时间
            loginUser.getUser().setPwdUpdateDate(DateUtils.getNowDate());
            loginUser.getUser().setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception
    {
        if (!file.isEmpty())
        {
            LoginUser loginUser = getLoginUser();
            String avatar = FileUploadUtils.upload(QuicoConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION, true);
            if (userService.updateUserAvatar(loginUser.getUserId(), avatar))
            {
                String oldAvatar = loginUser.getUser().getAvatar();
                if (StringUtils.isNotEmpty(oldAvatar))
                {
                    FileUtils.deleteFile(QuicoConfig.getProfile() + FileUtils.stripPrefix(oldAvatar));
                }
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
