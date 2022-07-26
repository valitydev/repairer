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
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final InvoicingSrv.Iface invoicingClient;
    private final ManagementSrv.Iface withdrawalManagementClient;
    private final MachineNamespaceProperties namespaceProperties;

    @SneakyThrows
    public String getProviderId(LifecycleEvent source) {
        String machineId = source.getMachineId();
        String machineNs = source.getMachineNs();
        if (machineNs.equals(namespaceProperties.getInvoicingNs())) {
            Invoice invoice = invoicingClient.get(machineId, null);
            if (invoice.isSetPayments() && invoice.getPayments().size() > 0) {
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
        return null;
    }
}
