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

import de.codecentric.spring.boot.chaos.monkey.component.MetricEventPublisher;
import de.codecentric.spring.boot.chaos.monkey.component.MetricType;
import de.codecentric.spring.boot.chaos.monkey.configuration.AssaultProperties;
import de.codecentric.spring.boot.chaos.monkey.configuration.ChaosMonkeySettings;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LatencyAssaultRangeTest {

    @Captor
    private ArgumentCaptor<AtomicInteger> captorTimeoutValue;

    @Test
    public void fixedLatencyIsPossible() {
        final int fixedLatency = 1000;

        checkLatencyConfiguration(fixedLatency, fixedLatency, is(fixedLatency));
    }

    @Test
    public void latencyRangeIsPossible() {
        final int latencyRangeStart = 1000;
        final int latencyRangeEnd = 5000;

        checkLatencyConfiguration(latencyRangeStart, latencyRangeEnd,
                is(both(greaterThan(latencyRangeStart)).and(lessThan(latencyRangeEnd))));
    }

    private void checkLatencyConfiguration(int latencyRangeStart, int latencyRangeEnd, Matcher<Integer> expectedResult) {
        final AssaultProperties assaultProperties = new AssaultProperties();
        assaultProperties.setLatencyRangeStart(latencyRangeStart);
        assaultProperties.setLatencyRangeEnd(latencyRangeEnd);

        final ChaosMonkeySettings chaosMonkeySettings = mock(ChaosMonkeySettings.class);
        when(chaosMonkeySettings.getAssaultProperties()).thenReturn(assaultProperties);

        final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        doNothing().when(publisher).publishEvent(any(ApplicationEvent.class));

        final MetricEventPublisher metricEventPublisher = spy(new MetricEventPublisher());
        metricEventPublisher.setApplicationEventPublisher(publisher);

        final LatencyAssault latencyAssault = new LatencyAssault(chaosMonkeySettings, metricEventPublisher);
        latencyAssault.attack();

        verify(metricEventPublisher).publishMetricEvent(eq(MetricType.LATENCY_ASSAULT), captorTimeoutValue.capture());
        assertThat(captorTimeoutValue.getValue().get(), expectedResult);
    }

}