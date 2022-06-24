package dev.vality.repairer.config;

import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.withdrawal_session.ManagementSrv;
import dev.vality.fistful.withdrawal_session.RepairerSrv;
import dev.vality.machinegun.stateproc.AutomatonSrv;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class AppConfig {
    @Bean
    public InvoicingSrv.Iface invoicingManagementClient(
            @Value("${service.invoicing.url}") Resource resource,
            @Value("${service.invoicing.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(InvoicingSrv.Iface.class);
    }

    @Bean
    public ManagementSrv.Iface withdrawalManagementClient(
            @Value("${service.withdrawal_managenent.url}") Resource resource,
            @Value("${service.withdrawal_managenent.networkTimeout}") int networkTimeout) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(ManagementSrv.Iface.class);
    }

    @Bean
    public InvoicingSrv.AsyncIface invoicingRepairClient(
            @Value("${service.invoicing.url}") Resource resource,
            @Value("${service.invoicing.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(InvoicingSrv.AsyncIface.class);
    }

    @Bean
    public RepairerSrv.AsyncIface withdrawalRepairClient(
            @Value("${service.withdrawal_repair.url}") Resource resource,
            @Value("${service.withdrawal_repair.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(RepairerSrv.AsyncIface.class);
    }

    @Bean
    public AutomatonSrv.AsyncIface machinegunClient(
            @Value("${service.machinegun_repair.url}") Resource resource,
            @Value("${service.machinegun_repair.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(AutomatonSrv.AsyncIface.class);
    }
}
