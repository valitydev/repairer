package dev.vality.repairer.service;

import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.repairer.config.properties.MachineNamespaceProperties;
import dev.vality.repairer.converter.MachineEventToMachineConverter;
import dev.vality.repairer.dao.MachineDao;
import dev.vality.repairer.domain.tables.pojos.Machine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MachineEventService {

    private final MachineNamespaceProperties namespaceProperties;
    private final MachineEventToMachineConverter converter;
    private final MachineDao machineDao;

    public void processEvent(LifecycleEvent lifecycleEvent) {
        if (accept(lifecycleEvent)) {
            Machine machine = converter.convert(lifecycleEvent);
            machineDao.save(machine).ifPresent(id -> machineDao.updateCurrent(machine, id));
        }
    }

    private boolean accept(LifecycleEvent lifecycleEvent) {
        String machineNs = lifecycleEvent.getMachineNs();
        return (machineNs.equals(namespaceProperties.getInvoicingNs())
                || machineNs.equals(namespaceProperties.getWithdrawalSessionNs()))
                && lifecycleEvent.getData().getMachine().isSetStatusChanged();
    }
}
