package studying.vyshkat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.vyshkat.service.FleetService;
import studying.vyshkat.service.ServiceCenter;
import studying.vyshkat.service.TechnicalServiceCenter;

/** Здесь Spring создаёт сервисы и связывает их между собой. */
@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {
    @Bean
    public ServiceCenter serviceCenter() {
        return new TechnicalServiceCenter();
    }

    @Bean
    public FleetService fleetService(ServiceCenter serviceCenter) {
        return new FleetService(serviceCenter);
    }
}
