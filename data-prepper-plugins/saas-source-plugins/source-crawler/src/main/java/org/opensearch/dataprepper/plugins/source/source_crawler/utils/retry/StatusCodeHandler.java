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

/**
 * Handler for determining retry behavior based on HTTP status codes
 */
public interface StatusCodeHandler {
    /**
     * Handle an HTTP exception and determine whether to retry
     *
     * @param ex                The HTTP exception
     * @param retryCount        Current retry attempt
     * @param credentialRenewal Runnable to renew credentials
     * @return RetryDecision indicating whether to stop/continue and optional
     *         exception
     */
    RetryDecision handleStatusCode(Exception ex, int retryCount, Runnable credentialRenewal);

    /**
     * Handle an HTTP exception and determine whether to retry with optional request context.
     *
     * <p>Default implementation preserves backward compatibility by delegating to
     * {@link #handleStatusCode(Exception, int, Runnable)}.
     */
    default RetryDecision handleStatusCode(
            final Exception ex,
            final int retryCount,
            final Runnable credentialRenewal,
            final RetryRequestContext retryRequestContext) {
        return handleStatusCode(ex, retryCount, credentialRenewal);
    }
}