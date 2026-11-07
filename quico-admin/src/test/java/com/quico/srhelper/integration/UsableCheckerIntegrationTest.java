package com.quico.srhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.quico.QuicoApplication;
import com.quico.srhelper.service.helper.UsableChecker;

/**
 * 集成测试：真实 Spring 容器 + test 库，验证 UsableChecker 的查询结果
 */
@SpringBootTest(classes = QuicoApplication.class)
@ActiveProfiles("test")
class UsableCheckerIntegrationTest {

    @Autowired
    private UsableChecker usableChecker;

    /**
     * 材料绑定完整（绑定行数 = 8）的角色 ID 集合应非空，且元素均为合法正数 ID
     */
    @Test
    void shouldReturnUsableCharacterIds() {
        Set<Long> characterIds = usableChecker.getUsableCharacterIds();

        assertThat(characterIds)
                .as("材料绑定完整的角色 ID 集合不应为空")
                .isNotEmpty();
        assertThat(characterIds)
                .as("角色 ID 不应包含 null 或非正数")
                .allMatch(id -> id != null && id > 0);
    }
}
