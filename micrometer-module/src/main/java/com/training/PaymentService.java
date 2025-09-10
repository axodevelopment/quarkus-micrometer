package com.training;

import jakarta.inject.Inject;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.annotation.Timed;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Random;

@ApplicationScoped
class PaymentService {
    private static final Random RND = new Random();

    @Inject MeterRegistry registry;

    @WithSpan("payment.authorize")
    void authorize(@SpanAttribute("sku") String sku,
                   @SpanAttribute("qty") int qty,
                   @SpanAttribute("amount") double amount) {

        Span.current().setAttribute("phase", "payment");

        if (RND.nextInt(6) == 0) {

            Span.current().recordException(new IllegalStateException("gateway-timeout"));
            Span.current().setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "gateway-timeout");
            
            throw new RuntimeException("payment gateway timeout");
        }

        sleep(30 + RND.nextInt(120));
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
