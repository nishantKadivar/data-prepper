/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 */

package org.opensearch.dataprepper.plugins.source.source_crawler.utils.retry;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class RetryAfterHeaderStrategyCompatibilityTest {

    @Test
    void constructors_ShouldRemainAvailableForDefaultStrategyCompatibility() {
        final RetryAfterHeaderStrategy defaultStrategy = new RetryAfterHeaderStrategy();
        final RetryAfterHeaderStrategy withMaxRetries = new RetryAfterHeaderStrategy(3);
        final RetryAfterHeaderStrategy withRateLimitConfig =
                new RetryAfterHeaderStrategy(List.of(2, 4), List.of(HttpStatus.TOO_MANY_REQUESTS));

        assertThat(defaultStrategy, notNullValue());
        assertThat(withMaxRetries.getMaxRetries(), equalTo(3));
        assertThat(withRateLimitConfig.getMaxRetries(), equalTo(2));
    }

    @Test
    void calculateSleepTime_ShouldUseDefaultRetryAfterHeaderBehavior() {
        final RetryAfterHeaderStrategy strategy = new RetryAfterHeaderStrategy(
                List.of(2, 4),
                List.of(HttpStatus.TOO_MANY_REQUESTS));
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", "7");
        final HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", headers, null, null);

        final long sleepTime = strategy.calculateSleepTime(exception, 0);

        assertThat(sleepTime, equalTo(7000L));
    }
}
