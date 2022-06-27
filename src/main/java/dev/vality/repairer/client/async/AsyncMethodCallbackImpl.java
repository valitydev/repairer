package dev.vality.repairer.client.async;

import dev.vality.repairer.dao.MachineDao;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.async.AsyncMethodCallback;

@RequiredArgsConstructor
public abstract class AsyncMethodCallbackImpl implements AsyncMethodCallback<Void> {

    protected final String id;
    protected final String namespace;
    protected final MachineDao machineDao;

    @Override
    public void onComplete(Void unused) {
        machineDao.updateInProgress(id, namespace, false);
    }
}
