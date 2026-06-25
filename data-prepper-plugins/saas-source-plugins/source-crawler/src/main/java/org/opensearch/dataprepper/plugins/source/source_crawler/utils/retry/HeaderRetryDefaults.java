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

import java.util.Arrays;
import java.util.List;

/**
 * Shared defaults for header-aware retry strategies.
 */
public final class HeaderRetryDefaults {
    public static final String REMAINING_HEADER_NAME = "X-RateLimit-Remaining";
    public static final String RESET_HEADER_NAME = "X-RateLimit-Reset";
    public static final String RETRY_DELAY_HEADER_NAME = "Retry-After";
    public static final int MAX_RETRY_DELAY_SECONDS = 86400;
    public static final List<HttpStatus> RATE_LIMIT_STATUS_CODES = Arrays.asList(HttpStatus.TOO_MANY_REQUESTS);
    public static final List<String> RETRY_DELAY_HEADERS = List.of(RETRY_DELAY_HEADER_NAME);

    private HeaderRetryDefaults() {
    }
}
