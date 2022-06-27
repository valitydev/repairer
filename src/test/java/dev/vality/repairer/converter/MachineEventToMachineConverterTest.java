package dev.vality.repairer.converter;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.lifesink.*;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.domain.tables.pojos.Machine;
import dev.vality.repairer.service.ProviderService;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

public class MachineEventToMachineConverterTest {

    @Mock
    private ProviderService providerService;

    private MachineEventToMachineConverter converter;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        converter = new MachineEventToMachineConverter(providerService);
    }

    @Test
    public void testConvertWorking() {
        LifecycleEvent source = RandomBeans.randomThrift(LifecycleEvent.class);
        source.getData().getMachine().setStatusChanged(
                new MachineLifecycleStatusChangedEvent()
                        .setNewStatus(MachineStatus.working(new MachineStatusWorking())));
        Machine machine = converter.convert(source);
        assertEquals(source.getMachineId(), machine.getMachineId());
        assertEquals(source.getMachineNs(), machine.getNamespace());
        assertEquals(TypeUtil.stringToLocalDateTime(source.getCreatedAt()), machine.getCreatedAt());
        assertEquals(Status.repaired, machine.getStatus());
    }

    @Test
    public void testConvertFailed() {
        LifecycleEvent source = RandomBeans.randomThrift(LifecycleEvent.class);
        source.getData().getMachine().setStatusChanged(
                new MachineLifecycleStatusChangedEvent()
                        .setNewStatus(MachineStatus.failed(new MachineStatusFailed().setReason("reason"))));
        Machine machine = converter.convert(source);
        assertEquals(source.getMachineId(), machine.getMachineId());
        assertEquals(source.getMachineNs(), machine.getNamespace());
        assertEquals(TypeUtil.stringToLocalDateTime(source.getCreatedAt()), machine.getCreatedAt());
        assertEquals(Status.failed, machine.getStatus());
    }
}