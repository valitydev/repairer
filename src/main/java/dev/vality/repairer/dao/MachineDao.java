package dev.vality.repairer.dao;

import dev.vality.repairer.SearchRequest;
import dev.vality.repairer.domain.tables.pojos.Machine;

import java.util.List;
import java.util.Optional;

public interface MachineDao {

    Optional<Long> save(Machine machine);

    void updateCurrent(Machine machine, Long id);

    List<dev.vality.repairer.Machine> search(SearchRequest request);

}
