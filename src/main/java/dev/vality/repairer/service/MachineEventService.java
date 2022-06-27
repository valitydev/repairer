package dev.vality.repairer.service;

import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.repairer.config.properties.MachineNamespaceProperties;
import dev.vality.repairer.converter.MachineEventToMachineConverter;
import dev.vality.repairer.dao.MachineDao;
import dev.vality.repairer.domain.tables.pojos.Machine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
            log.info("Event processed: {}", lifecycleEvent);
        }
    }

    private boolean accept(LifecycleEvent lifecycleEvent) {
        String machineNs = lifecycleEvent.getMachineNs();
        return (machineNs.equals(namespaceProperties.getInvoicingNs())
                || machineNs.equals(namespaceProperties.getWithdrawalSessionNs()))
                && lifecycleEvent.getData().getMachine().isSetStatusChanged();
    }
}
