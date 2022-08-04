package dev.vality.repairer.handler;

import dev.vality.damsel.base.InvalidRequest;
import dev.vality.damsel.payment_processing.InvoiceNotFound;
import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.MachineAlreadyWorking;
import dev.vality.fistful.WithdrawalSessionNotFound;
import dev.vality.fistful.withdrawal_session.RepairerSrv;
import dev.vality.machinegun.stateproc.*;
import dev.vality.repairer.*;
import dev.vality.repairer.Machine;
import dev.vality.repairer.config.properties.MachineNamespaceProperties;
import dev.vality.repairer.dao.MachineDao;
import dev.vality.repairer.service.TokenGenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepairManagement implements RepairManagementSrv.Iface {

    private final MachineDao machineDao;
    private final TokenGenService tokenGenService;
    private final RepairerSrv.Iface withdrawalRepairClient;
    private final InvoicingSrv.Iface invoicingClient;
    private final AutomatonSrv.Iface machinegunClient;
    private final MachineNamespaceProperties namespaces;

    @Override
    public void simpleRepairAll(List<MachineSimpleRepairRequest> requests) throws TException {
        log.info("Simple repair machines {}", toString(requests));
        requests.forEach(request -> {
            machineDao.updateInProgress(request.getId(), request.getNs(), true);
            CompletableFuture.runAsync(() -> {
                try {
                    machinegunClient.simpleRepair(request.getNs(), Reference.id(request.getId()));
                } catch (NamespaceNotFound e) {
                    log.info("Machine namespace not found: {}, {}", request.getId(), request.getNs());
                } catch (MachineNotFound e) {
                    log.info("Machine not found: {}, {}", request.getId(), request.getNs());
                } catch (MachineFailed e) {
                    log.info("Machine failed: {}, {}", request.getId(), request.getNs());
                } catch (dev.vality.machinegun.stateproc.MachineAlreadyWorking e) {
                    log.info("Machine {}, {} is already working", request.getId(), request.getNs());
                    machineDao.updateInProgress(request.getId(), request.getNs(), false);
                } catch (TException e) {
                    log.warn("Unexpected exception", e);
                }
            });
        });
    }

    @Override
    public void repairWithdrawals(List<RepairWithdrawalRequest> requests) throws TException {
        log.info("Repair withdrawals {}", toString(requests));
        requests.forEach(request -> {
            machineDao.updateInProgress(request.getId(), namespaces.getWithdrawalSessionNs(), true);
            CompletableFuture.runAsync(() -> {
                try {
                    withdrawalRepairClient.repair(request.getId(), request.getScenario());
                    machineDao.updateInProgress(request.getId(), namespaces.getWithdrawalSessionNs(), false);
                } catch (WithdrawalSessionNotFound e) {
                    log.info("Withdrawal session not found: {}", request.getId());
                } catch (MachineAlreadyWorking e) {
                    log.info("Withdrawal {} is already working", request.getId());
                    machineDao.updateInProgress(request.getId(), namespaces.getWithdrawalSessionNs(), false);
                } catch (TException e) {
                    log.warn("Unexpected exception", e);
                }
            });
        });
    }

    @Override
    public void repairInvoices(List<RepairInvoiceRequest> requests) throws TException {
        log.info("Repair invoices {}", toString(requests));
        requests.forEach(request -> {
            machineDao.updateInProgress(request.getId(), namespaces.getInvoicingNs(), true);
            CompletableFuture.runAsync(() -> {
                try {
                    invoicingClient.repairWithScenario(request.getId(), request.getScenario());
                    machineDao.updateInProgress(request.getId(), namespaces.getInvoicingNs(), false);
                } catch (InvoiceNotFound e) {
                    log.info("Invoice not found: {}", request.getId());
                } catch (InvalidRequest e) {
                    log.info("Invalid request: {}", request.getId());
                } catch (TException e) {
                    log.warn("Unexpected exception", e);
                }
            });
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
