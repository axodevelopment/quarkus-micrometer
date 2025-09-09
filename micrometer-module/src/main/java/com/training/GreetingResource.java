package com.training;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.annotation.Timed;

import org.jboss.logging.Logger;

@Path("/hello")
public class GreetingResource {

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);

    @Inject
    MeterRegistry registry;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        LOG.info("hello: MicroMeter");
        registry.counter("micrometer.counter", "type", "add").increment();
        return "MicroMeter endpoint";
    }

    @Timed(value = "micrometer.greeting.timer")
    public String timer() {
        return "OK";
    }
}
