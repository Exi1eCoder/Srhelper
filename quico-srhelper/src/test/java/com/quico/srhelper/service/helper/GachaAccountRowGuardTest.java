package com.quico.srhelper.service.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quico.common.exception.ServiceException;
import com.quico.srhelper.domain.SrGachaRecord;
import com.quico.srhelper.domain.enums.ViolationTypeEnum;
import com.quico.srhelper.exception.UploadValidateException;

/**
 * 解析阶段账号守卫单元测试
 *
 * @author quico
 */
@ExtendWith(MockitoExtension.class)
class GachaAccountRowGuardTest {

    private static final String UID = "12345612";
    private static final Long USER_ID = 1L;

    @Mock
    private GachaAccountBindingService bindingService;

    private GachaAccountRowGuard newGuard() {
        return new GachaAccountRowGuard(bindingService, USER_ID);
    }

    private static SrGachaRecord record(String uid) {
        return SrGachaRecord.builder().uid(uid).build();
    }

    @Test
    @DisplayName("首行提取基准 uid 并预检，后续同 uid 行不重复预检")
    void shouldPreCheckOnceWithTrimmedUid() {
        GachaAccountRowGuard guard = newGuard();

        guard.accept(record(" " + UID + " "));
        guard.accept(record(UID));

        assertEquals(UID, guard.getBaseUid());
        verify(bindingService, times(1)).preCheck(UID, USER_ID);
    }

    @Test
    @DisplayName("首行 uid 为空 → FORMAT_ERROR，且不做归属预检")
    void shouldRejectEmptyUid() {
        GachaAccountRowGuard guard = newGuard();

        assertThrows(UploadValidateException.class, () -> guard.accept(record("  ")));
        assertNull(guard.getBaseUid());
        verify(bindingService, never()).preCheck(any(), any());
    }

    @Test
    @DisplayName("第二行 uid 变化 → MULTI_UID_ATTACK")
    void shouldRejectMultiUid() {
        GachaAccountRowGuard guard = newGuard();
        guard.accept(record(UID));

        UploadValidateException ex = assertThrows(UploadValidateException.class,
                () -> guard.accept(record("99999912")));
        assertEquals(ViolationTypeEnum.MULTI_UID_ATTACK, ex.getViolationType());
    }

    @Test
    @DisplayName("归属预检抛出的业务异常原样传播（不被当作解析失败）")
    void shouldPropagateServiceExceptionFromPreCheck() {
        GachaAccountRowGuard guard = newGuard();
        doThrow(new ServiceException("已绑定", 40301)).when(bindingService).preCheck(any(), any());

        assertThrows(ServiceException.class, () -> guard.accept(record(UID)));
    }

    @Test
    @DisplayName("同一 uid 多行 → 无异常")
    void shouldPassWhenSameUid() {
        GachaAccountRowGuard guard = newGuard();

        assertDoesNotThrow(() -> {
            guard.accept(record(UID));
            guard.accept(record(UID));
            guard.accept(record(UID));
        });
        verify(bindingService, times(1)).preCheck(UID, USER_ID);
    }
}
