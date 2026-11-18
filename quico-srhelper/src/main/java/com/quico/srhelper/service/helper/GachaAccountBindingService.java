package com.quico.srhelper.service.helper;

import com.quico.common.constant.CacheConstants;
import com.quico.common.exception.ServiceException;
import com.quico.srhelper.domain.SrUserUid;
import com.quico.srhelper.mapper.SrUserUidMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 游戏账号归属服务（先到先得）
 *
 * 规则：
 * - 未被绑定 → 当前用户抢注成功
 * - 已被他人绑定 → 40301，阻断并引导申诉
 * - 已被自己绑定 → 放行
 * - 并发抢注撞唯一索引 → 40302，允许稍后重试
 *
 * 归属冲突属于业务规则，不是文件违规：
 * 一律抛 ServiceException，禁止使用 UploadValidateException，
 * 避免被第五层风控当成用户违规写入审计并累加失败次数。
 *
 * @author quico
 */
@Slf4j
@Service
public class GachaAccountBindingService
{
    /** 40301 账号已被其他用户绑定 */
    public static final int CODE_ACCOUNT_BOUND = 40301;

    /** 40302 并发抢注冲突，可稍后重试 */
    public static final int CODE_ACCOUNT_BINDING_CONFLICT = 40302;

    /** 40303 单用户绑定数量超过上限 */
    public static final int CODE_BIND_LIMIT_EXCEEDED = 40303;

    /** 统一话术：已被他人绑定 */
    public static final String MSG_ACCOUNT_BOUND = "该游戏账号已绑定其他系统用户，请联系管理员提供相关凭证进行申诉。";

    /** 统一话术：并发抢注冲突 */
    public static final String MSG_ACCOUNT_BINDING_CONFLICT = "该账号正在被其他用户绑定，请稍后重试。";

    /** 统一话术：绑定数量超限（占位符为上限值） */
    public static final String MSG_BIND_LIMIT_EXCEEDED = "当前账号最多可绑定 %d 个游戏账号，如需增加请联系管理员。";

    /** 单用户绑定上限系统参数键 */
    private static final String CONFIG_BIND_LIMIT = "gacha.upload.uid.bind.limit";

    /** 系统参数缺失或非法时的兜底上限 */
    private static final long DEFAULT_BIND_LIMIT = 3;

    @Autowired
    private SrUserUidMapper userUidMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 事务外只读预检，快速失败
     *
     * @param uid 游戏账号UID（已 trim）
     * @param userId 当前登录用户ID
     */
    public void preCheck(String uid, Long userId)
    {
        SrUserUid bound = userUidMapper.selectByUid(uid);
        if (bound != null)
        {
            if (!bound.getUserId().equals(userId))
            {
                log.warn("游戏账号 {} 已被用户 {} 绑定，当前用户 {}", uid, bound.getUserId(), userId);
                throw new ServiceException(MSG_ACCOUNT_BOUND, CODE_ACCOUNT_BOUND);
            }
            // 已绑定在自己名下：不占用新的绑定名额
            return;
        }
        assertBindQuota(userId);
    }

    /**
     * 绑定当前用户，必须在入库事务内调用
     *
     * 并发抢注时数据库唯一索引只允许一个事务提交；
     * 若撞索引后发现绑定者就是自己（重复提交），按幂等成功处理
     *
     * @param uid 游戏账号UID（已 trim）
     * @param userId 当前登录用户ID
     */
    public void bind(String uid, Long userId)
    {
        SrUserUid bound = userUidMapper.selectByUid(uid);
        if (bound != null)
        {
            if (bound.getUserId().equals(userId))
            {
                return;
            }
            throw new ServiceException(MSG_ACCOUNT_BOUND, CODE_ACCOUNT_BOUND);
        }

        try
        {
            assertBindQuota(userId);
            userUidMapper.insertSrUserUid(SrUserUid.builder().uid(uid).userId(userId).build());
        }
        catch (DuplicateKeyException e)
        {
            // 并发抢注：唯一索引已由另一个事务占用
            SrUserUid winner = userUidMapper.selectByUid(uid);
            if (winner != null && winner.getUserId().equals(userId))
            {
                return;
            }
            log.warn("游戏账号 {} 并发抢注冲突，当前用户 {}", uid, userId);
            throw new ServiceException(MSG_ACCOUNT_BINDING_CONFLICT, CODE_ACCOUNT_BINDING_CONFLICT);
        }
    }

    /**
     * 校验当前用户已绑定的游戏账号数量是否达到上限
     *
     * 上限由系统参数 gacha.upload.uid.bind.limit 维护：
     * - 未配置或读取失败：默认 3 个
     * - 配置为 0 或负数：不限制
     *
     * @param userId 当前登录用户ID
     */
    private void assertBindQuota(Long userId)
    {
        long limit = bindLimit();
        if (limit <= 0)
        {
            return;
        }
        long boundCount = userUidMapper.countByUserId(userId);
        if (boundCount >= limit)
        {
            log.warn("用户 {} 已绑定 {} 个游戏账号，达到上限 {}", userId, boundCount, limit);
            throw new ServiceException(String.format(MSG_BIND_LIMIT_EXCEEDED, limit), CODE_BIND_LIMIT_EXCEEDED);
        }
    }

    /**
     * 读取单用户绑定上限，缺失或非法时回退默认值，避免配置异常导致功能不可用
     */
    private long bindLimit()
    {
        try
        {
            Object value = redisTemplate.opsForValue().get(CacheConstants.SYS_CONFIG_KEY + CONFIG_BIND_LIMIT);
            if (value != null)
            {
                return Long.parseLong(value.toString().trim());
            }
        }
        catch (Exception e)
        {
            log.warn("读取系统参数 {} 失败，使用默认 {} 个", CONFIG_BIND_LIMIT, DEFAULT_BIND_LIMIT, e);
        }
        return DEFAULT_BIND_LIMIT;
    }
}
