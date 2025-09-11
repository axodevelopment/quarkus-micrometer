package com.training;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.*;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.concurrent.atomic.AtomicInteger;

@Path("/work")
@ApplicationScoped
public class WorkResource {
    @Inject
    MeterRegistry registry;
    @Inject
    DownstreamService downstream;

    private final AtomicInteger inFlight = new AtomicInteger(0);
    private final Counter retries;

    public WorkResource(MeterRegistry reg) {
        this.retries = Counter.builder("app.work.retries").register(reg);

        reg.gauge("app.work.in_flight", inFlight);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed(value = "app.work.duration", histogram = true, percentiles = { 0.5, 0.95 })
    @WithSpan("work.doWork")
    public String doWork() {
        inFlight.incrementAndGet();

        try {
            if (!downstream.callWithRetry(retries)) {
                throw new WebApplicationException("downstream-failed", 502);
            }

            return "ok";
        } catch(WebApplicationException ex) {
          return "fail";
        } finally {
            inFlight.decrementAndGet();
        }
    }
}
