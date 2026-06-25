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
 * Generic wrapper over {@link RetryAfterHeaderStrategy} with neutral constructor
 * parameter naming for custom header mappings.
 */
public class HeaderAwareRetryStrategy extends RetryAfterHeaderStrategy {
    private final List<HttpStatus> rateLimitStatusCodes;
    private final List<String> retryDelayHeaderNames;
    private final String remainingHeaderName;
    private final String resetHeaderName;

    /**
     * Constructor with default sleep times.
     */
    public HeaderAwareRetryStrategy() {
        super();
        this.rateLimitStatusCodes = HeaderRetryDefaults.RATE_LIMIT_STATUS_CODES;
        this.retryDelayHeaderNames = HeaderRetryDefaults.RETRY_DELAY_HEADERS;
        this.remainingHeaderName = HeaderRetryDefaults.REMAINING_HEADER_NAME;
        this.resetHeaderName = HeaderRetryDefaults.RESET_HEADER_NAME;
    }

    /**
     * Constructor with custom max retries.
     *
     * @param maxRetries maximum number of retries
     */
    public HeaderAwareRetryStrategy(final int maxRetries) {
        super(maxRetries);
        this.rateLimitStatusCodes = HeaderRetryDefaults.RATE_LIMIT_STATUS_CODES;
        this.retryDelayHeaderNames = HeaderRetryDefaults.RETRY_DELAY_HEADERS;
        this.remainingHeaderName = HeaderRetryDefaults.REMAINING_HEADER_NAME;
        this.resetHeaderName = HeaderRetryDefaults.RESET_HEADER_NAME;
    }

    /**
     * Constructor with custom sleep times and custom rate-limit status codes.
     *
     * @param rateLimitRetrySleepTime custom sleep times for rate-limit retries (in seconds)
     * @param rateLimitStatusCodes status codes considered rate-limited
     */
    public HeaderAwareRetryStrategy(
            final List<Integer> rateLimitRetrySleepTime,
            final List<HttpStatus> rateLimitStatusCodes) {
        this(rateLimitRetrySleepTime, rateLimitStatusCodes, HeaderRetryDefaults.RETRY_DELAY_HEADERS,
            HeaderRetryDefaults.REMAINING_HEADER_NAME, HeaderRetryDefaults.RESET_HEADER_NAME);
    }

    /**
     * Constructor with custom header mapping.
     *
     * @param rateLimitRetrySleepTime custom sleep times for rate-limit retries (in seconds)
     * @param rateLimitStatusCodes status codes considered rate-limited
     * @param retryDelayHeaderNames retry-delay style headers in precedence order.
     *                              Any provided header names are respected as-is.
     * @param remainingHeaderName remaining-quota header name
     * @param resetHeaderName reset-epoch header name
     */
    public HeaderAwareRetryStrategy(
            final List<Integer> rateLimitRetrySleepTime,
            final List<HttpStatus> rateLimitStatusCodes,
            final List<String> retryDelayHeaderNames,
            final String remainingHeaderName,
            final String resetHeaderName) {
        super(rateLimitRetrySleepTime, rateLimitStatusCodes);
        this.rateLimitStatusCodes = rateLimitStatusCodes != null
                ? List.copyOf(rateLimitStatusCodes)
                : HeaderRetryDefaults.RATE_LIMIT_STATUS_CODES;
        this.retryDelayHeaderNames = retryDelayHeaderNames != null && !retryDelayHeaderNames.isEmpty()
                ? List.copyOf(retryDelayHeaderNames)
                : HeaderRetryDefaults.RETRY_DELAY_HEADERS;
        this.remainingHeaderName = remainingHeaderName != null && !remainingHeaderName.isBlank()
                ? remainingHeaderName
                : HeaderRetryDefaults.REMAINING_HEADER_NAME;
        this.resetHeaderName = resetHeaderName != null && !resetHeaderName.isBlank()
                ? resetHeaderName
                : HeaderRetryDefaults.RESET_HEADER_NAME;
    }

    @Override
    protected boolean isRateLimited(final HttpStatus status) {
        return rateLimitStatusCodes.contains(status);
    }

    @Override
    protected List<String> getRetryDelayHeaderNames() {
        return retryDelayHeaderNames;
    }

    @Override
    protected String getRemainingHeaderName() {
        return remainingHeaderName;
    }

    @Override
    protected String getResetHeaderName() {
        return resetHeaderName;
    }
}
