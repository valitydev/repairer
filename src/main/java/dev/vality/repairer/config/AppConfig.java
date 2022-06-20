package dev.vality.repairer.config;

import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.withdrawal_session.ManagementSrv;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class AppConfig {
    @Bean
    public InvoicingSrv.Iface invoicingClient(@Value("${service.invoicing.url}") Resource resource,
                                              @Value("${service.invoicing.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(InvoicingSrv.Iface.class);
    }

    @Bean
    public ManagementSrv.Iface withdrawalClient(@Value("${service.fistful.url}") Resource resource,
                                                @Value("${service.fistful.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(ManagementSrv.Iface.class);
    }
}
