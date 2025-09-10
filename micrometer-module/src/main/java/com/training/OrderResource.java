package com.training;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.PathParam;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.annotation.Timed;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Duration;

import org.jboss.logging.Logger;

@Path("/order")
public class OrderResource {

    private static final Logger LOG = Logger.getLogger(OrderResource.class);

    @Inject MeterRegistry registry;
    @Inject InventoryService inventory;
    @Inject PaymentService payment;

    private final AtomicInteger inFlight = new AtomicInteger(0);
    private final Counter outcomesSuccess;
    private final Counter outcomesError;
    private final DistributionSummary respBytes;

    public OrderResource(MeterRegistry reg) {
        this.outcomesSuccess = Counter.builder("app.order.outcomes")
                .tag("outcome", "success").register(reg);

        this.outcomesError = Counter.builder("app.order.outcomes")
                .tag("outcome", "error").register(reg);

        this.respBytes = DistributionSummary.builder("app.order.response_bytes")
                .baseUnit("bytes").publishPercentileHistogram().register(reg);

        reg.gauge("app.order.in_flight", inFlight);
    }
    
    @GET
    @Path("/{sku}")
    @Produces(MediaType.TEXT_PLAIN)
    @Timed(value = "app.order.duration", histogram = true, percentiles = {0.5, 0.95, 0.99})
    public String place(@PathParam("sku") String sku, @QueryParam("qty") @DefaultValue("1") int qty) {
        
        inFlight.incrementAndGet();
        long start = System.nanoTime();
        
        try {

            // Task 2: child spans + attributes make the slow phase obvious in Jaeger
            boolean ok = inventory.fetchStock(sku, qty);
            if (!ok) throw new WebApplicationException("out-of-stock", 409);

            payment.authorize(sku, qty, computeAmount(qty));

            String body = "OK " + sku + " x" + qty;
            respBytes.record(body.getBytes(StandardCharsets.UTF_8).length);
            outcomesSuccess.increment();
            return body;
        } catch (RuntimeException e) {
            outcomesError.increment();
            throw e;
        } finally {
            inFlight.decrementAndGet();
            long durNanos = System.nanoTime() - start;
            LOG.infof("order %s qty=%d took %s ms", sku, qty, Duration.ofNanos(durNanos).toMillis());
        }
    }

    private double computeAmount(int qty) {
        return 9.99 * qty;
    }
}
