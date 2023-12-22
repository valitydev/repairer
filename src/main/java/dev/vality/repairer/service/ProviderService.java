package dev.vality.repairer.service;

import dev.vality.damsel.payment_processing.Invoice;
import dev.vality.damsel.payment_processing.InvoicePayment;
import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.base.EventRange;
import dev.vality.fistful.withdrawal_session.ManagementSrv;
import dev.vality.fistful.withdrawal_session.SessionState;
import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.repairer.config.properties.MachineNamespaceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {

    private final InvoicingSrv.Iface invoicingClient;
    private final ManagementSrv.Iface withdrawalManagementClient;
    private final MachineNamespaceProperties namespaceProperties;

    public String getProviderId(LifecycleEvent source) {
        String machineId = source.getMachineId();
        String machineNs = source.getMachineNs();

        try {
            if (machineNs.equals(namespaceProperties.getInvoicingNs())) {
                Invoice invoice = invoicingClient.get(machineId, null);
                if (invoice.isSetPayments() && !invoice.getPayments().isEmpty()) {
                    InvoicePayment payment = invoice.getPayments().get(invoice.getPayments().size() - 1);
                    if (payment.isSetRoute()) {
                        return String.valueOf(payment.getRoute().getProvider().getId());
                    }
                }
            } else if (machineNs.equals(namespaceProperties.getWithdrawalSessionNs())) {
                SessionState sessionState = withdrawalManagementClient.get(machineId, new EventRange());
                if (sessionState.isSetRoute()) {
                    return String.valueOf(sessionState.getRoute().getProviderId());
                }
            }
        } catch (TException e) {
            log.warn("Unable to get providerId for machineId={}, machineNs={}", machineId, machineNs, e);
        }

        return null;
    }
}
