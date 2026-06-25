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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.opensearch.dataprepper.logging.DataPrepperMarkers.NOISY;

/**
 * Retry strategy that derives retry delay from retry-after and rate-limit style headers.
 */
@Slf4j
public class RetryAfterHeaderStrategy implements RetryStrategy {
    private final List<Integer> retryAttemptSleepTime;
    private final List<Integer> rateLimitRetrySleepTime;
    private final List<HttpStatus> rateLimitStatusCodes;
    private final int maxRetries;

    /**
     * Constructor with default sleep times
     */
    public RetryAfterHeaderStrategy() {
        this.retryAttemptSleepTime = RetryStrategy.DEFAULT_RETRY_ATTEMPT_SLEEP_TIME;
        this.rateLimitRetrySleepTime = RetryStrategy.DEFAULT_RATE_LIMIT_RETRY_SLEEP_TIME;
        this.rateLimitStatusCodes = HeaderRetryDefaults.RATE_LIMIT_STATUS_CODES;
        this.maxRetries = RetryStrategy.MAX_RETRIES;
    }

    /**
     * Constructor with custom max retries
     *
     * @param maxRetries Maximum number of retries
     */
    public RetryAfterHeaderStrategy(final int maxRetries) {
        this.retryAttemptSleepTime = RetryStrategy.DEFAULT_RETRY_ATTEMPT_SLEEP_TIME;
        this.rateLimitRetrySleepTime = RetryStrategy.DEFAULT_RATE_LIMIT_RETRY_SLEEP_TIME;
        this.rateLimitStatusCodes = HeaderRetryDefaults.RATE_LIMIT_STATUS_CODES;
        this.maxRetries = maxRetries;
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
        this.retryAttemptSleepTime = RetryStrategy.DEFAULT_RETRY_ATTEMPT_SLEEP_TIME;
        this.rateLimitRetrySleepTime = rateLimitRetrySleepTime != null
                ? rateLimitRetrySleepTime
                : RetryStrategy.DEFAULT_RATE_LIMIT_RETRY_SLEEP_TIME;
        this.rateLimitStatusCodes = rateLimitStatusCodes != null
                ? rateLimitStatusCodes
                : HeaderRetryDefaults.RATE_LIMIT_STATUS_CODES;
        this.maxRetries = this.rateLimitRetrySleepTime.size();
    }

    @Override
    public long calculateSleepTime(final Exception ex, final int retryCount) {
        final Optional<HttpStatus> statusCode = RetryStrategy.getStatusCode(ex);

        if (statusCode.isPresent() && isRateLimited(statusCode.get())) {
            final Optional<Integer> retryDelaySeconds = extractRetryDelayFromHeaders(ex);
            if (retryDelaySeconds.isPresent()) {
                log.info("Using retry-delay header value: {} seconds (attempt {}/{})",
                        retryDelaySeconds.get(), retryCount + 1, getMaxRetries());
                return retryDelaySeconds.get() * RetryStrategy.SLEEP_TIME_MULTIPLIER_MS;
            }
        }

        final List<Integer> sleepTimes = (statusCode.isPresent() && isRateLimited(statusCode.get()))
                ? rateLimitRetrySleepTime
                : retryAttemptSleepTime;

        final int sleepTimeSeconds = (retryCount < sleepTimes.size())
                ? sleepTimes.get(retryCount)
                : sleepTimes.get(sleepTimes.size() - 1);

        log.debug("Retrying in {} seconds (attempt {}/{})",
                sleepTimeSeconds, retryCount + 1, getMaxRetries());

        return sleepTimeSeconds * RetryStrategy.SLEEP_TIME_MULTIPLIER_MS;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    protected boolean isRateLimited(final HttpStatus status) {
        return rateLimitStatusCodes.contains(status);
    }

    protected List<String> getRetryDelayHeaderNames() {
        return HeaderRetryDefaults.RETRY_DELAY_HEADERS;
    }

    protected String getRemainingHeaderName() {
        return HeaderRetryDefaults.REMAINING_HEADER_NAME;
    }

    protected String getResetHeaderName() {
        return HeaderRetryDefaults.RESET_HEADER_NAME;
    }

    private Optional<Integer> extractRetryDelayFromHeaders(final Exception ex) {
        try {
            HttpHeaders headers = null;
            if (ex instanceof HttpClientErrorException) {
                headers = ((HttpClientErrorException) ex).getResponseHeaders();
            } else if (ex instanceof HttpServerErrorException) {
                headers = ((HttpServerErrorException) ex).getResponseHeaders();
            }

            if (headers != null) {
                for (final String retryDelayHeaderName : getRetryDelayHeaderNames()) {
                    final String retryDelayValue = headers.getFirst(retryDelayHeaderName);
                    if (retryDelayValue != null) {
                        final double parsedSeconds = Double.parseDouble(retryDelayValue);
                        if (Double.isFinite(parsedSeconds)) {
                            final int seconds = (int) Math.min(Math.ceil(parsedSeconds), HeaderRetryDefaults.MAX_RETRY_DELAY_SECONDS);
                            return Optional.of(Math.max(seconds, 1));
                        }
                    }
                }
            }

            if (headers != null
                    && headers.containsKey(getRemainingHeaderName())
                    && headers.containsKey(getResetHeaderName())) {
                final String remaining = headers.getFirst(getRemainingHeaderName());
                final String resetEpoch = headers.getFirst(getResetHeaderName());
                if (remaining != null && remaining.equals("0") && resetEpoch != null && !resetEpoch.isBlank()) {
                    final long resetSeconds = Long.parseLong(resetEpoch);
                    final long nowSeconds = Instant.now().getEpochSecond();
                    final long wait = resetSeconds - nowSeconds + 1;
                    return Optional.of((int) Math.max(wait, 1));
                }
            }
        } catch (NumberFormatException e) {
            log.warn(NOISY, "Failed to parse retry-delay header: {}", e.getMessage());
        }

        return Optional.empty();
    }
}