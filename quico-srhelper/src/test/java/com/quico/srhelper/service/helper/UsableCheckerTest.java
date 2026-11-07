package com.quico.srhelper.service.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quico.srhelper.mapper.SrCharacterMaterialBindMapper;
import com.quico.srhelper.mapper.SrItemMapper;
import com.quico.srhelper.mapper.SrLightconeMaterialBindMapper;
import com.quico.srhelper.service.helper.UsableChecker;

/**
 * UsableChecker 纯单元测试
 */
@ExtendWith(MockitoExtension.class)
class UsableCheckerTest {

    @Mock
    private SrCharacterMaterialBindMapper characterBindMapper;

    @Mock
    private SrLightconeMaterialBindMapper lightconeBindMapper;

    @Mock
    private SrItemMapper itemMapper;

    @InjectMocks
    private UsableChecker usableChecker;

    /**
     * 用意：Mapper 返回重复 ID 时，结果应转成去重后的 Set，并确认确实调用了一次 Mapper 方法
     */
    @Test
    void shouldConvertMapperResultToSetWithoutDuplicates() {
        when(characterBindMapper.selectUsableCharacterIds())
                .thenReturn(Arrays.asList(128L, 129L, 129L, 22L));

        Set<Long> result = usableChecker.getUsableCharacterIds();

        assertEquals(Set.of(128L, 129L, 22L), result);
        verify(characterBindMapper).selectUsableCharacterIds();
    }

    /**
     * 用意：Mapper 返回空列表（无任何绑定完整的角色）时，应返回空集合而不是 null，避免调用方 NPE
     */
    @Test
    void shouldReturnEmptySetWhenMapperReturnsEmptyList() {
        when(characterBindMapper.selectUsableCharacterIds()).thenReturn(Collections.emptyList());

        Set<Long> result = usableChecker.getUsableCharacterIds();

        assertTrue(result.isEmpty());
    }

    /**
     * 用意：Mapper 返回 null 时，toSet 的兜底分支应返回空集合，保证下游 contains 判断不抛空指针
     */
    @Test
    void shouldReturnEmptySetWhenMapperReturnsNull() {
        when(characterBindMapper.selectUsableCharacterIds()).thenReturn(null);

        Set<Long> result = usableChecker.getUsableCharacterIds();

        assertTrue(result.isEmpty());
    }
}
