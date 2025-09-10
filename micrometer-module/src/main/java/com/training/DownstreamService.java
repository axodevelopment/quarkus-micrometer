package com.training;

import jakarta.enterprise.context.ApplicationScoped;
import io.micrometer.core.instrument.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import java.util.Random;

@ApplicationScoped
class DownstreamService {

	private static final Random RND = new Random();
	private static final int maxRetries = 3;
	private static final int backoffMs = 100;

	@WithSpan("downstream.call")
	public boolean callWithRetry(Counter retryCounter) {
		
		Span span = Span.current();
		int attempt = 1;

		while (true) {
			boolean ok = attemptOnce(attempt);

			span.addEvent("attempt", Attributes.of(
					longKey("attempt"), (long) attempt,
					stringKey("outcome"), ok ? "ok" : "fail"));

			if (ok) {
				span.setAttribute("attempts_total", attempt);

				return true;
			}

			if (attempt >= maxRetries + 1) {
				span.setAttribute("attempts_total", attempt);
				span.setStatus(StatusCode.ERROR, "retry-exhausted");

				return false;
			}

			retryCounter.increment();
			span.addEvent("retry", Attributes.of(
					AttributeKey.longKey("next_attempt"), (long) (attempt + 1),
					AttributeKey.longKey("backoff_ms"), (long) backoffMs,
					AttributeKey.stringKey("reason"), "transient"));

			sleep(backoffMs);
			attempt++;
		}
	}

	@WithSpan("downstream.attempt")
	boolean attemptOnce(@SpanAttribute("attempt") int attempt) {

		Span span = Span.current();

		sleep(40 + RND.nextInt(40));
		int rnd = RND.nextInt(3);
		boolean logic = rnd >= 1;

		span.setAttribute("attempt.logic", logic);

		if (logic) {
			Span.current().setAttribute("error.kind", "transient");

			return false;
		}

		return true;
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}
}