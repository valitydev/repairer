package dev.vality.repairer.converter;

import dev.vality.damsel.payment_processing.*;
import dev.vality.fistful.base.EventRange;
import dev.vality.fistful.withdrawal_session.ManagementSrv;
import dev.vality.fistful.withdrawal_session.SessionState;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.machinegun.lifesink.MachineStatus;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.domain.tables.pojos.Machine;
import dev.vality.repairer.util.MachineUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MachineEventToMachineConverter implements Converter<LifecycleEvent, Machine> {

    private static final String DEFAULT_PAYMENT_ID = "1"; // cringe
    private final InvoicingSrv.Iface invoicingClient;
    private final ManagementSrv.Iface withdrawalClient;

    @Override
    public Machine convert(LifecycleEvent source) {
        Machine result = new Machine();
        result.setMachineId(source.getMachineId());
        result.setNamespace(source.getMachineNs());
        result.setCreatedAt(TypeUtil.stringToLocalDateTime(source.getCreatedAt()));
        result.setStatus(getStatus(source));
        result.setErrorMessage(getErrorMessage(source));
        result.setProviderId(getProviderId(source));
        return result;
    }

    @SneakyThrows
    private String getProviderId(LifecycleEvent source) {
        String machineId = source.getMachineId();
        String machineNs = source.getMachineNs();
        if (MachineUtil.isInvoicing(machineNs)) {
            InvoicePayment payment = invoicingClient.getPayment(machineId, DEFAULT_PAYMENT_ID); //TODO
            if (payment.isSetRoute()) {
                return String.valueOf(payment.getRoute().getProvider().getId());
            }
        } else if (MachineUtil.isWithdrawal(machineNs)) {
            SessionState sessionState = withdrawalClient.get(machineId, new EventRange()); //TODO
            if (sessionState.isSetRoute()) {
                return String.valueOf(sessionState.getRoute().getProviderId());
            }
        }
        return null;
    }

    private String getErrorMessage(LifecycleEvent source) {
        MachineStatus newStatus = source.getData().getMachine().getStatusChanged().getNewStatus();
        if (newStatus.isSetFailed()) {
            return newStatus.getFailed().getReason();
        }
        return null;
    }

    private Status getStatus(LifecycleEvent source) {
        MachineStatus newStatus = source.getData().getMachine().getStatusChanged().getNewStatus();
        if (newStatus.isSetFailed()) {
            return Status.failed;
        } else if (newStatus.isSetWorking()) {
            return Status.repaired;
        } else {
            throw new UnsupportedOperationException("Unknown status " + newStatus);
        }
    }
}
