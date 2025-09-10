package com.training;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.annotation.Timed;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;



import org.jboss.logging.Logger;

@Path("/hello")
public class GreetingResource {

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);

    @Inject
    MeterRegistry registry;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @WithSpan
    public String hello() {
        LOG.info("hello: MicroMeter");
        registry.counter("micrometer.counter", "type", "add").increment();

        DoWork("process-type");

        return "Hello from the MicroMeter endpoint";
    }

    @WithSpan
    public boolean DoWork(@SpanAttribute(value = "test") String workType) {
        try {
            Span span = Span.current();

            try (Scope scope = span.makeCurrent()) {
                // Add attributes to the span
                span.setAttribute("order.id", 12345);


                // Simulate some work
                Thread.sleep(50);
            } catch (InterruptedException e) {
                span.setStatus(StatusCode.ERROR, "Interrupted");
                return false;
            }


            Thread.sleep(50);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Timed(value = "micrometer.greeting.timer")
    public String timer() {
        return "OK";
    }
}
