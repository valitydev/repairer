package dev.vality.repairer.handler;

import dev.vality.damsel.base.InvalidRequest;
import dev.vality.damsel.payment_processing.InvoiceNotFound;
import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.MachineAlreadyWorking;
import dev.vality.fistful.WithdrawalSessionNotFound;
import dev.vality.fistful.withdrawal_session.RepairerSrv;
import dev.vality.machinegun.stateproc.AutomatonSrv;
import dev.vality.machinegun.stateproc.Reference;
import dev.vality.repairer.*;
import dev.vality.repairer.dao.MachineDao;
import dev.vality.repairer.service.TokenGenService;
import dev.vality.repairer.util.MachineUtil;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RepairManagement implements RepairManagementSrv.Iface {

    private final MachineDao machineDao;
    private final TokenGenService tokenGenService;
    private final RepairerSrv.Iface withdrawalRepairClient;
    private final InvoicingSrv.Iface invoicingClient;
    private final AutomatonSrv.Iface machinegunClient;

    @Override
    public List<MachineRepairResponse> simpleRepairAll(List<MachineSimpleRepairRequest> requests) throws TException {
        return requests.stream().map(r -> {
            var response = new MachineRepairResponse()
                    .setId(r.getId())
                    .setNs(r.getNs())
                    .setStatus(RepairStatus.in_progress);
            try {
                machinegunClient.simpleRepair(r.getNs(), Reference.id(r.getId()));
            } catch (dev.vality.machinegun.stateproc.NamespaceNotFound |
                    dev.vality.machinegun.stateproc.MachineNotFound e) {
                response.setStatus(RepairStatus.failed).setErrorMessage("NotFound");
            } catch (dev.vality.machinegun.stateproc.MachineFailed e) {
                response.setStatus(RepairStatus.failed).setErrorMessage("MachineFailed");
            } catch (dev.vality.machinegun.stateproc.MachineAlreadyWorking e) {
                response.setStatus(RepairStatus.failed).setErrorMessage("MachineAlreadyWorking");
            } catch (TException e) {
                response.setStatus(RepairStatus.failed).setErrorMessage(e.getMessage());
            }
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MachineRepairResponse> repairWithdrawals(List<RepairWithdrawalRequest> requests) throws TException {
        return requests.stream().map(r -> {
            var response = new MachineRepairResponse()
                    .setId(r.getId())
                    .setNs(MachineUtil.WITHDRAWAL_NAMESPACE)
                    .setStatus(RepairStatus.in_progress);
            try {
                withdrawalRepairClient.repair(r.getId(), r.getScenario());
            } catch (WithdrawalSessionNotFound e) {
                response.setStatus(RepairStatus.failed).setErrorMessage("WithdrawalSessionNotFound");
            } catch (MachineAlreadyWorking e) {
                response.setStatus(RepairStatus.failed).setErrorMessage("MachineAlreadyWorking");
            } catch (TException e) {
                response.setStatus(RepairStatus.failed).setErrorMessage(e.getMessage());
            }
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MachineRepairResponse> repairInvoices(List<RepairInvoiceRequest> requests) throws TException {
        return requests.stream().map(r -> {
            var response = new MachineRepairResponse()
                    .setId(r.getId())
                    .setNs(MachineUtil.INVOICE_NAMESPACE)
                    .setStatus(RepairStatus.in_progress);
            try {
                invoicingClient.repairWithScenario(r.getId(), r.getScenario());
            } catch (InvoiceNotFound e) {
                response.setStatus(RepairStatus.failed).setErrorMessage("InvoiceNotFound");
            } catch (InvalidRequest e) {
                response.setStatus(RepairStatus.failed).setErrorMessage("InvalidRequest");
            } catch (TException e) {
                response.setStatus(RepairStatus.failed).setErrorMessage(e.getMessage());
            }
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public SearchResponse search(SearchRequest request) throws TException {
        var requestWithNullToken = new SearchRequest(request);
        requestWithNullToken.setContinuationToken(null);
        tokenGenService.validateToken(requestWithNullToken, request.getContinuationToken());
        List<Machine> machines = machineDao.search(request);
        String continuationToken = tokenGenService.generateToken(requestWithNullToken, machines);
        return new SearchResponse()
                .setMachines(machines)
                .setContinuationToken(continuationToken);
    }
}
