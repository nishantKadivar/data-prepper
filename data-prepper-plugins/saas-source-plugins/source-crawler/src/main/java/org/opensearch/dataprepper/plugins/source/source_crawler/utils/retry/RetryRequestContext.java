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

import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * Optional request context for retry decision and sleep strategy evaluation.
 */
@Value
@Builder
public class RetryRequestContext {
    public static final RetryRequestContext EMPTY = RetryRequestContext.builder().build();

    String endpoint;
    @Builder.Default
    Map<String, String> metadata = Collections.emptyMap();
}
