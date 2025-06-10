package dev.vality.repairer.converter;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.machinegun.lifesink.MachineLifecycleStatusChangedEvent;
import dev.vality.machinegun.stateproc.MachineStatus;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.domain.tables.pojos.Machine;
import dev.vality.repairer.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MachineEventToMachineConverter implements Converter<LifecycleEvent, Machine> {

    private final ProviderService providerService;

    @Override
    public Machine convert(LifecycleEvent source) {
        Machine result = new Machine();
        result.setMachineId(source.getMachineId());
        result.setNamespace(source.getMachineNs());
        result.setCreatedAt(TypeUtil.stringToLocalDateTime(source.getCreatedAt()));
        result.setStatus(getStatus(source));
        result.setErrorMessage(getErrorMessage(source));
        result.setProviderId(providerService.getProviderId(source));
        return result;
    }


    private String getErrorMessage(LifecycleEvent source) {
        MachineStatus newStatus = source.getData().getMachine().getStatusChanged().getNewStatus();
        if (newStatus.isSetFailed()) {
            return newStatus.getFailed().getReason();
        }
        return null;
    }

    private Status getStatus(LifecycleEvent source) {
        MachineLifecycleStatusChangedEvent statusChanged = source.getData().getMachine().getStatusChanged();
        MachineStatus newStatus = statusChanged.getNewStatus();
        if (newStatus.isSetFailed()) {
            return Status.failed;
        } else if (newStatus.isSetWorking()) {
            return Status.repaired;
        } else {
            throw new UnsupportedOperationException("Unknown status " + newStatus);
        }
    }
}
