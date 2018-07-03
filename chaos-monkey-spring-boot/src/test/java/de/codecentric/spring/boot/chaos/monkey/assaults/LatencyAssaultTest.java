/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.spring.boot.chaos.monkey.assaults;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Thorsten Deelmann
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LatencyAssault.class, RandomUtils.class})
public class LatencyAssaultTest {

    @Test
    public void threadSleepHasBeenCalled() throws Exception {
        mockStatic(Thread.class);
        mockStatic(RandomUtils.class);
        int sleepTimeMillis = 150;
        int latencyRangeStart = 100;
        int latencyRangeStop = 200;
        when(RandomUtils.nextInt(latencyRangeStart, latencyRangeStop)).thenReturn(sleepTimeMillis);

        LatencyAssault latencyAssault = new LatencyAssault(latencyRangeStart, latencyRangeStop, true);
        latencyAssault.attack();

        verifyStatic(Thread.class, times(1));
        Thread.sleep(sleepTimeMillis);
    }
}