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

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * @deprecated Prefer {@link HeaderAwareRetryStrategy}. This class is retained
 * only for backward compatibility.
 */
@Deprecated
public class RetryAfterHeaderStrategy extends HeaderAwareRetryStrategy {

    /**
     * Constructor with default sleep times
     */
    public RetryAfterHeaderStrategy() {
        super();
    }

    /**
     * Constructor with custom max retries
     *
     * @param maxRetries Maximum number of retries
     */
    public RetryAfterHeaderStrategy(final int maxRetries) {
        super(maxRetries);
    }

    /**
     * Constructor with Custom sleep times for rate limit retries and custom rate limit status codes
     *
     * @param rateLimitRetrySleepTime Custom sleep times for rate limit retries (in
     *                                seconds)
     * @param rateLimitStatusCodes List of status codes that are considered rate limited
     */
    public RetryAfterHeaderStrategy(
            final List<Integer> rateLimitRetrySleepTime,
            final List<HttpStatus> rateLimitStatusCodes) {
        super(rateLimitRetrySleepTime, rateLimitStatusCodes);
    }

    /**
     * Constructor with custom header mapping.
     *
     * @param rateLimitRetrySleepTime custom sleep times for rate-limit retries (in seconds)
     * @param rateLimitStatusCodes status codes considered rate-limited
     * @param retryDelayHeaderNames retry-delay style headers in precedence order
     * @param remainingHeaderName remaining-quota header name
     * @param resetHeaderName reset-epoch header name
     */
    public RetryAfterHeaderStrategy(
            final List<Integer> rateLimitRetrySleepTime,
            final List<HttpStatus> rateLimitStatusCodes,
            final List<String> retryDelayHeaderNames,
            final String remainingHeaderName,
            final String resetHeaderName) {
        super(
                rateLimitRetrySleepTime,
                rateLimitStatusCodes,
                retryDelayHeaderNames,
                remainingHeaderName,
                resetHeaderName);
    }
}