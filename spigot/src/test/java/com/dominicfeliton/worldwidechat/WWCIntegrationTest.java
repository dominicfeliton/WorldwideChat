package com.dominicfeliton.worldwidechat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

abstract class WWCIntegrationTest {

    @BeforeAll
    static void startMockBukkit() {
        WWCTestSupport.start();
    }

    @AfterEach
    void resetPluginState() {
        WWCTestSupport.reset();
    }

    WorldwideChat plugin() {
        return WWCTestSupport.plugin();
    }
}
