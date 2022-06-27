package dev.vality.repairer.client.async;

import dev.vality.fistful.MachineAlreadyWorking;
import dev.vality.fistful.WithdrawalSessionNotFound;
import dev.vality.repairer.dao.MachineDao;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WithdrawalCallbackHandler extends AsyncMethodCallbackImpl {

    public WithdrawalCallbackHandler(String id, String namespace, MachineDao machineDao) {
        super(id, namespace, machineDao);
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof WithdrawalSessionNotFound) {
            log.info("Withdrawal session not found: {}", id);
        } else if (e instanceof MachineAlreadyWorking) {
            log.info("Withdrawal {} is already working", id);
            machineDao.updateInProgress(id, namespace, false);
        } else {
            log.warn("Unknown state of machine {}", id);
        }
    }
}
