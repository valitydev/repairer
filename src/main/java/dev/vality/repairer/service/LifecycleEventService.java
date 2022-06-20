package dev.vality.repairer.service;

import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.repairer.converter.LifecycleEventToMachineConverter;
import dev.vality.repairer.domain.tables.pojos.Machine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LifecycleEventService {

    private final LifecycleEventToMachineConverter converter;

    public void processEvent(LifecycleEvent lifecycleEvent) {
        if (isStatusChanged(lifecycleEvent)) {
            Machine machineData = converter.convert(lifecycleEvent);
        }
    }

    private boolean isStatusChanged(LifecycleEvent lifecycleEvent) {
        return lifecycleEvent.getData().getMachine().isSetStatusChanged();
    }
}
