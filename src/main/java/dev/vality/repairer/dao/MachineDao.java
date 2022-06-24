package dev.vality.repairer.dao;

import dev.vality.repairer.SearchRequest;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.domain.tables.pojos.Machine;

import java.util.List;
import java.util.Optional;

public interface MachineDao {

    Optional<Long> save(Machine machine);

    void updateCurrent(Machine machine, Long id);

    void updateStatus(String machineId, String namespace, Status status, String errorMessage);

    List<dev.vality.repairer.Machine> search(SearchRequest request);

}
