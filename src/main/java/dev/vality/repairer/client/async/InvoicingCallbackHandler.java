package dev.vality.repairer.client.async;

import dev.vality.damsel.base.InvalidRequest;
import dev.vality.damsel.payment_processing.InvoiceNotFound;
import dev.vality.repairer.dao.MachineDao;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvoicingCallbackHandler extends AsyncMethodCallbackImpl {

    public InvoicingCallbackHandler(String id, String namespace, MachineDao machineDao) {
        super(id, namespace, machineDao);
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof InvoiceNotFound) {
            log.info("Invoice not found: {}", id);
        } else if (e instanceof InvalidRequest) {
            log.info("Invalid request: {}", id);
        } else {
            log.warn("Unknown state of machine {}", id);
        }
    }
}
