package dev.vality.repairer.client.async;

import dev.vality.machinegun.stateproc.MachineAlreadyWorking;
import dev.vality.machinegun.stateproc.MachineFailed;
import dev.vality.machinegun.stateproc.MachineNotFound;
import dev.vality.machinegun.stateproc.NamespaceNotFound;
import dev.vality.repairer.dao.MachineDao;
import dev.vality.repairer.domain.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.async.AsyncMethodCallback;

@Slf4j
@RequiredArgsConstructor
public class MachinegunCallbackHandler implements AsyncMethodCallback<Void> {

    private final String id;
    private final String namespace;
    private final MachineDao machineDao;

    @Override
    public void onComplete(Void unused) {
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof NamespaceNotFound) {
            machineDao.updateStatus(id, namespace, Status.failed, "NamespaceNotFound");
        } else if (e instanceof MachineNotFound) {
            machineDao.updateStatus(id, namespace, Status.failed, "MachineNotFound");
        } else if (e instanceof MachineFailed) {
            machineDao.updateStatus(id, namespace, Status.failed, "MachineFailed");
        } else if (e instanceof MachineAlreadyWorking) {
            log.info("Machine {} is already working", id);
            machineDao.updateStatus(id, namespace, Status.repaired, null);
        } else {
            log.warn("Unknown state of machine {}", id);
        }
    }
}
