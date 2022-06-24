package dev.vality.repairer.client.async;

import dev.vality.damsel.base.InvalidRequest;
import dev.vality.damsel.payment_processing.InvoiceNotFound;
import dev.vality.repairer.dao.MachineDao;
import dev.vality.repairer.domain.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.async.AsyncMethodCallback;

@Slf4j
@RequiredArgsConstructor
public class InvoicingCallbackHandler implements AsyncMethodCallback<Void> {

    private final String id;
    private final String namespace;
    private final MachineDao machineDao;

    @Override
    public void onComplete(Void unused) {
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof InvoiceNotFound) {
            machineDao.updateStatus(id, namespace, Status.failed, "InvoiceNotFound");
        } else if (e instanceof InvalidRequest) {
            machineDao.updateStatus(id, namespace, Status.failed, "InvalidRequest");
        } else {
            log.warn("Unknown state of machine {}", id);
        }
    }
}
