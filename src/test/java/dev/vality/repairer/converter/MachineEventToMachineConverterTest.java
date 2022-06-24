package dev.vality.repairer.converter;

import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.withdrawal_session.ManagementSrv;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.lifesink.LifecycleEvent;
import dev.vality.machinegun.lifesink.MachineLifecycleStatusChangedEvent;
import dev.vality.machinegun.lifesink.MachineStatus;
import dev.vality.repairer.config.properties.MachineNamespaceProperties;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.domain.tables.pojos.Machine;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

public class MachineEventToMachineConverterTest {

    @Mock
    private InvoicingSrv.Iface invoicingClient;

    @Mock
    private ManagementSrv.Iface withdrawalManagementClient;

    private MachineEventToMachineConverter converter;

    @BeforeEach
    public void beforeEach() {
        converter = new MachineEventToMachineConverter(
                invoicingClient, withdrawalManagementClient, new MachineNamespaceProperties());
    }

    @Test
    public void convert() {
        LifecycleEvent source = RandomBeans.randomThrift(LifecycleEvent.class);
        source.getData().getMachine().setStatusChanged(
                RandomBeans.randomThrift(MachineLifecycleStatusChangedEvent.class));
        Machine machine = converter.convert(source);
        assertEquals(source.getMachineId(), machine.getMachineId());
        assertEquals(source.getMachineNs(), machine.getNamespace());
        assertEquals(TypeUtil.stringToLocalDateTime(source.getCreatedAt()), machine.getCreatedAt());
        MachineStatus newStatus = source.getData().getMachine().getStatusChanged().getNewStatus();
        if (newStatus.isSetFailed()) {
            assertEquals(Status.failed, machine.getStatus());
            assertEquals(newStatus.getFailed().getReason(), machine.getErrorMessage());
        } else if (source.getData().getMachine().getStatusChanged().getNewStatus().isSetWorking()) {
            assertEquals(Status.repaired, machine.getStatus());
        }
    }
}