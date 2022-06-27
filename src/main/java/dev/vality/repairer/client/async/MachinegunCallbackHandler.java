package dev.vality.repairer.client.async;

import dev.vality.machinegun.stateproc.MachineAlreadyWorking;
import dev.vality.machinegun.stateproc.MachineFailed;
import dev.vality.machinegun.stateproc.MachineNotFound;
import dev.vality.machinegun.stateproc.NamespaceNotFound;
import dev.vality.repairer.dao.MachineDao;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MachinegunCallbackHandler extends AsyncMethodCallbackImpl {

    public MachinegunCallbackHandler(String id, String namespace, MachineDao machineDao) {
        super(id, namespace, machineDao);
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof NamespaceNotFound) {
            log.info("Machine namespace not found: {}, {}", id, namespace);
        } else if (e instanceof MachineNotFound) {
            log.info("Machine not found: {}, {}", id, namespace);
        } else if (e instanceof MachineFailed) {
            log.info("Machine failed: {}, {}", id, namespace);
        } else if (e instanceof MachineAlreadyWorking) {
            log.info("Machine {}, {} is already working", id, namespace);
            machineDao.updateInProgress(id, namespace, false);
        } else {
            log.warn("Unknown state of machine {}", id);
        }
    }
}
