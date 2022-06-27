package dev.vality.repairer.handler;

import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.withdrawal_session.RepairerSrv;
import dev.vality.machinegun.stateproc.AutomatonSrv;
import dev.vality.machinegun.stateproc.Reference;
import dev.vality.repairer.*;
import dev.vality.repairer.client.async.InvoicingCallbackHandler;
import dev.vality.repairer.client.async.MachinegunCallbackHandler;
import dev.vality.repairer.client.async.WithdrawalCallbackHandler;
import dev.vality.repairer.config.properties.MachineNamespaceProperties;
import dev.vality.repairer.dao.MachineDao;
import dev.vality.repairer.service.TokenGenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepairManagement implements RepairManagementSrv.Iface {

    private final MachineDao machineDao;
    private final TokenGenService tokenGenService;
    private final RepairerSrv.AsyncIface withdrawalRepairClient;
    private final InvoicingSrv.AsyncIface invoicingClient;
    private final AutomatonSrv.AsyncIface machinegunClient;
    private final MachineNamespaceProperties namespaces;

    @Override
    public void simpleRepairAll(List<MachineSimpleRepairRequest> requests) throws TException {
        log.info("Simple repair machines {}", toString(requests));
        requests.forEach(request -> {
            machineDao.updateInProgress(request.getId(), request.getNs(), true);
            try {
                machinegunClient.simpleRepair(request.getNs(), Reference.id(request.getId()),
                        new MachinegunCallbackHandler(request.getId(), request.getNs(), machineDao));
            } catch (TException e) {
                log.warn("Unexpected exception", e);
            }
        });
    }

    @Override
    public void repairWithdrawals(List<RepairWithdrawalRequest> requests) throws TException {
        log.info("Repair withdrawals {}", toString(requests));
        requests.forEach(request -> {
            machineDao.updateInProgress(request.getId(), namespaces.getWithdrawalSessionNs(), true);
            try {
                withdrawalRepairClient.repair(request.getId(), request.getScenario(),
                        new WithdrawalCallbackHandler(request.getId(),
                                namespaces.getWithdrawalSessionNs(), machineDao));
            } catch (TException e) {
                log.warn("Unexpected exception", e);
            }
        });
    }

    @Override
    public void repairInvoices(List<RepairInvoiceRequest> requests) throws TException {
        log.info("Repair invoices {}", toString(requests));
        requests.forEach(request -> {
            machineDao.updateInProgress(request.getId(), namespaces.getInvoicingNs(), true);
            try {
                invoicingClient.repairWithScenario(request.getId(), request.getScenario(),
                        new InvoicingCallbackHandler(request.getId(), namespaces.getWithdrawalSessionNs(), machineDao));
            } catch (TException e) {
                log.warn("Unexpected exception", e);
            }
        });
    }

    @Override
    public SearchResponse search(SearchRequest request) throws TException {
        log.info("Search request {}", request);
        var requestWithNullToken = new SearchRequest(request);
        requestWithNullToken.setContinuationToken(null);
        tokenGenService.validateToken(requestWithNullToken, request.getContinuationToken());
        List<Machine> machines = machineDao.search(request);
        String continuationToken = tokenGenService.generateToken(requestWithNullToken, machines);
        SearchResponse searchResponse = new SearchResponse()
                .setMachines(machines)
                .setContinuationToken(continuationToken);
        log.info("Search response, size {}", searchResponse.getMachines().size());
        return searchResponse;
    }

    private <E> String toString(Collection<E> collection) {
        return collection.toString().substring(0, Math.min(collection.toString().length(), 1000));
    }
}
