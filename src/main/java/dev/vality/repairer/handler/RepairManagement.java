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
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.service.TokenGenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<MachineRepairResponse> simpleRepairAll(List<MachineSimpleRepairRequest> requests) throws TException {
        log.info("Simple repair machines {}", requests);
        return requests.stream().map(r -> {
            machineDao.updateStatus(r.getId(), r.getNs(), Status.in_progress, null);
            try {
                machinegunClient.simpleRepair(r.getNs(), Reference.id(r.getId()),
                        new MachinegunCallbackHandler(r.getId(), r.getNs(), machineDao));
            } catch (TException e) {
                log.warn("Unexpected exception", e);
            }
            return getResponseInProgress(r.getId(), r.getNs());
        }).collect(Collectors.toList());
    }

    @Override
    public List<MachineRepairResponse> repairWithdrawals(List<RepairWithdrawalRequest> requests) throws TException {
        log.info("Repair withdrawals {}", requests);
        return requests.stream().map(r -> {
            machineDao.updateStatus(r.getId(), namespaces.getWithdrawalSessionNs(), Status.in_progress, null);
            try {
                withdrawalRepairClient.repair(r.getId(), r.getScenario(),
                        new WithdrawalCallbackHandler(r.getId(), namespaces.getWithdrawalSessionNs(), machineDao));
            } catch (TException e) {
                log.warn("Unexpected exception", e);
            }
            return getResponseInProgress(r.getId(), namespaces.getWithdrawalSessionNs());
        }).collect(Collectors.toList());
    }

    @Override
    public List<MachineRepairResponse> repairInvoices(List<RepairInvoiceRequest> requests) throws TException {
        log.info("Repair invoices {}", requests);
        return requests.stream().map(r -> {
            machineDao.updateStatus(r.getId(), namespaces.getInvoicingNs(), Status.in_progress, null);
            try {
                invoicingClient.repairWithScenario(r.getId(), r.getScenario(),
                        new InvoicingCallbackHandler(r.getId(), namespaces.getWithdrawalSessionNs(), machineDao));
            } catch (TException e) {
                log.warn("Unexpected exception", e);
            }
            return getResponseInProgress(r.getId(), namespaces.getInvoicingNs());
        }).collect(Collectors.toList());
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

    private MachineRepairResponse getResponseInProgress(String id, String namespaces) {
        return new MachineRepairResponse()
                .setId(id)
                .setNs(namespaces)
                .setStatus(RepairStatus.in_progress);
    }
}
