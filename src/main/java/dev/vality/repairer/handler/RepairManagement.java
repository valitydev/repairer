package dev.vality.repairer.handler;

import dev.vality.repairer.*;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepairManagement implements RepairManagementSrv.Iface {
    @Override
    public List<MachineRepairResponse> simpleRepairAll(List<MachineSimpleRepairRequest> list) throws TException {
        return null;
    }

    @Override
    public List<MachineRepairResponse> repairWithdrawals(List<RepairWithdrawalRequest> list) throws TException {
        return null;
    }

    @Override
    public List<MachineRepairResponse> repairInvoices(List<RepairInvoiceRequest> list) throws TException {
        return null;
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws TException {
        return null;
    }
}
