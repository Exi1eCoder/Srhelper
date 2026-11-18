package com.quico.srhelper.service.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.quico.common.exception.ServiceException;
import com.quico.srhelper.domain.SrUserUid;
import com.quico.srhelper.mapper.SrUserUidMapper;

/**
 * 游戏账号归属服务单元测试（先到先得）
 *
 * @author quico
 */
@ExtendWith(MockitoExtension.class)
class GachaAccountBindingServiceTest {

    private static final String UID = "12345612";
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private SrUserUidMapper userUidMapper;

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    private ValueOperations<Object, Object> valueOperations;

    @InjectMocks
    private GachaAccountBindingService service;

    @BeforeEach
    void setUp() {
        // 系统参数未配置时走默认上限（3 个）
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(any())).thenReturn(null);
    }

    private static SrUserUid bound(Long userId) {
        return SrUserUid.builder().uid(UID).userId(userId).build();
    }

    private static void assertCode(int expected, ServiceException ex) {
        assertEquals(expected, ex.getCode());
    }

    @Nested
    @DisplayName("事务外只读预检")
    class PreCheck {

        @Test
        @DisplayName("无人绑定 → 放行且不写库")
        void shouldPassWhenUnbound() {
            when(userUidMapper.selectByUid(UID)).thenReturn(null);

            assertDoesNotThrow(() -> service.preCheck(UID, USER_ID));
            verify(userUidMapper, never()).insertSrUserUid(any());
        }

        @Test
        @DisplayName("已被自己绑定 → 放行")
        void shouldPassWhenBoundBySelf() {
            when(userUidMapper.selectByUid(UID)).thenReturn(bound(USER_ID));

            assertDoesNotThrow(() -> service.preCheck(UID, USER_ID));
        }

        @Test
        @DisplayName("已被他人绑定 → 40301")
        void shouldRejectWhenBoundByOther() {
            when(userUidMapper.selectByUid(UID)).thenReturn(bound(OTHER_USER_ID));

            ServiceException ex = assertThrows(ServiceException.class, () -> service.preCheck(UID, USER_ID));
            assertCode(GachaAccountBindingService.CODE_ACCOUNT_BOUND, ex);
        }
    }

    @Nested
    @DisplayName("事务内绑定")
    class Bind {

        @Test
        @DisplayName("无人绑定 → 插入绑定关系")
        void shouldInsertWhenUnbound() {
            when(userUidMapper.selectByUid(UID)).thenReturn(null);

            service.bind(UID, USER_ID);

            ArgumentCaptor<SrUserUid> captor = ArgumentCaptor.forClass(SrUserUid.class);
            verify(userUidMapper).insertSrUserUid(captor.capture());
            assertEquals(UID, captor.getValue().getUid());
            assertEquals(USER_ID, captor.getValue().getUserId());
        }

        @Test
        @DisplayName("已被自己绑定 → 幂等返回，不重复插入")
        void shouldSkipInsertWhenBoundBySelf() {
            when(userUidMapper.selectByUid(UID)).thenReturn(bound(USER_ID));

            assertDoesNotThrow(() -> service.bind(UID, USER_ID));
            verify(userUidMapper, never()).insertSrUserUid(any());
        }

        @Test
        @DisplayName("已被他人绑定 → 40301 且不插入")
        void shouldRejectWhenBoundByOther() {
            when(userUidMapper.selectByUid(UID)).thenReturn(bound(OTHER_USER_ID));

            ServiceException ex = assertThrows(ServiceException.class, () -> service.bind(UID, USER_ID));
            assertCode(GachaAccountBindingService.CODE_ACCOUNT_BOUND, ex);
            verify(userUidMapper, never()).insertSrUserUid(any());
        }

        @Test
        @DisplayName("并发撞唯一索引且赢家是他人 → 40302")
        void shouldRejectWhenConcurrentConflict() {
            when(userUidMapper.selectByUid(UID)).thenReturn(null, bound(OTHER_USER_ID));
            when(userUidMapper.insertSrUserUid(any())).thenThrow(new DuplicateKeyException("uk_uid"));

            ServiceException ex = assertThrows(ServiceException.class, () -> service.bind(UID, USER_ID));
            assertCode(GachaAccountBindingService.CODE_ACCOUNT_BINDING_CONFLICT, ex);
        }

        @Test
        @DisplayName("并发撞唯一索引但赢家是自己（重复提交）→ 幂等成功")
        void shouldPassWhenConcurrentWinnerIsSelf() {
            when(userUidMapper.selectByUid(UID)).thenReturn(null, bound(USER_ID));
            when(userUidMapper.insertSrUserUid(any())).thenThrow(new DuplicateKeyException("uk_uid"));

            assertDoesNotThrow(() -> service.bind(UID, USER_ID));
        }
    }

    @Nested
    @DisplayName("单用户绑定数量上限")
    class Quota {

        @Test
        @DisplayName("未达到上限 → 放行")
        void shouldPassWhenBelowLimit() {
            when(userUidMapper.selectByUid(UID)).thenReturn(null);
            when(userUidMapper.countByUserId(USER_ID)).thenReturn(2L);

            assertDoesNotThrow(() -> service.preCheck(UID, USER_ID));
        }

        @Test
        @DisplayName("达到默认上限 3 个 → 40303")
        void shouldRejectWhenLimitReached() {
            when(userUidMapper.selectByUid(UID)).thenReturn(null);
            when(userUidMapper.countByUserId(USER_ID)).thenReturn(3L);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.preCheck(UID, USER_ID));
            assertCode(GachaAccountBindingService.CODE_BIND_LIMIT_EXCEEDED, ex);
        }

        @Test
        @DisplayName("系统参数配置为 0 → 不限制")
        void shouldNotLimitWhenConfigIsZero() {
            when(valueOperations.get(any())).thenReturn("0");
            when(userUidMapper.selectByUid(UID)).thenReturn(null);
            when(userUidMapper.countByUserId(USER_ID)).thenReturn(10L);

            assertDoesNotThrow(() -> service.preCheck(UID, USER_ID));
        }

        @Test
        @DisplayName("系统参数非法 → 回退默认上限 3")
        void shouldFallbackToDefaultWhenConfigInvalid() {
            when(valueOperations.get(any())).thenReturn("abc");
            when(userUidMapper.selectByUid(UID)).thenReturn(null);
            when(userUidMapper.countByUserId(USER_ID)).thenReturn(3L);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.preCheck(UID, USER_ID));
            assertCode(GachaAccountBindingService.CODE_BIND_LIMIT_EXCEEDED, ex);
        }

        @Test
        @DisplayName("uid 已绑定在自己名下 → 不占用名额，不校验数量")
        void shouldSkipQuotaWhenBoundBySelf() {
            when(userUidMapper.selectByUid(UID)).thenReturn(bound(USER_ID));

            assertDoesNotThrow(() -> service.preCheck(UID, USER_ID));
            verify(userUidMapper, never()).countByUserId(any());
        }
    }
}
