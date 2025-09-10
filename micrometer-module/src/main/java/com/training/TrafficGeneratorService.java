package com.training;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TrafficGeneratorService {
  private static final Logger LOG = Logger.getLogger(TrafficGeneratorService.class);

  @Inject WorkResource work;

  @Scheduled(every = "2s", delayed = "3s", identity = "traffic")
  void createTraffic() {
    LOG.info("scheduler tick");

    try { 
        work.doWork(); 
    } catch (Exception e) { 
        LOG.warn("createTraffic failed", e);
    }
  }
}
