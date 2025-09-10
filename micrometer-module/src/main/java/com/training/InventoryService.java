package com.training;

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
class InventoryService {
    private static final Random RND = new Random();

    @WithSpan("inventory.fetch")
    public boolean fetchStock(@SpanAttribute("sku") String sku,
                       @SpanAttribute("qty") int qty) {
        Span.current().setAttribute("phase", "inventory");

        boolean cacheHit = RND.nextBoolean();

        if (!cacheHit) {
            Span.current().addEvent("cache.miss");
        }
        // "A12-LAG" is our mystery: add extra latency + jitter
        sleep(cacheHit ? 10 + RND.nextInt(20) : 60 + RND.nextInt(sku.equals("A12-LAG") ? 350 : 120));

        return true;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}