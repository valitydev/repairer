package dev.vality.repairer.dao;

import dev.vality.repairer.RepairStatus;
import dev.vality.repairer.SearchRequest;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.domain.tables.pojos.Machine;
import dev.vality.repairer.config.PostgresqlSpringBootITest;
import dev.vality.repairer.util.MapperUtil;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.*;

@PostgresqlSpringBootITest
public class MachineDaoTest {

    @Autowired
    private MachineDao machineDao;

    @Test
    public void save() {
        Machine source = random(Machine.class, "id", "current");
        Optional<Long> idOptional = machineDao.save(source);
        assertTrue(idOptional.isPresent());
        var result = machineDao.search(new SearchRequest());
        assertEquals(1, result.size());
        assertEquals(source.getMachineId(), result.get(0).getId());
    }

    @Test
    public void updateCurrent() {
        Machine source = random(Machine.class, "id", "current");
        Optional<Long> idOptional = machineDao.save(source);
        assertTrue(idOptional.isPresent());
        machineDao.updateCurrent(source, idOptional.get());
        var result = machineDao.search(new SearchRequest());
        assertFalse(result.isEmpty());
    }

    @Test
    public void updateInProgress() {
        Machine source = random(Machine.class, "id", "current");
        Optional<Long> idOptional = machineDao.save(source);
        assertTrue(idOptional.isPresent());
        machineDao.updateInProgress(source.getMachineId(), source.getNamespace(), true);
        var result = machineDao.search(new SearchRequest());
        assertEquals(1, result.size());
        assertEquals(RepairStatus.in_progress, result.get(0).getStatus());
    }

    @RepeatedTest(5)
    public void search() {
        Machine source = random(Machine.class, "id", "current", "inProgress");
        source.setStatus(Status.failed);
        System.out.println(source);
        machineDao.save(source);
        SearchRequest request = new SearchRequest()
                .setIds(List.of(source.getMachineId()))
                .setNs(source.getNamespace())
                .setStatus(MapperUtil.map(source.getStatus()))
                .setProviderId(source.getProviderId())
                .setErrorMessage(source.getErrorMessage());
        var result = machineDao.search(request);
        assertEquals(1, result.size());
        assertEquals(source.getMachineId(), result.get(0).getId());
    }

    @Test
    public void searchInProgress() {
        Machine source = random(Machine.class, "id", "current", "inProgress");
        machineDao.save(source);
        machineDao.updateInProgress(source.getMachineId(), source.getNamespace(), true);
        SearchRequest request = new SearchRequest()
                .setStatus(RepairStatus.in_progress);
        var result = machineDao.search(request);
        assertEquals(1, result.size());
    }

    @Test
    public void searchInProgressNotFound() {
        Machine source = random(Machine.class, "id", "current", "inProgress");
        machineDao.save(source);
        machineDao.updateInProgress(source.getMachineId(), source.getNamespace(), true);
        SearchRequest request = new SearchRequest()
                .setStatus(RepairStatus.failed);
        var result = machineDao.search(request);
        assertTrue(result.isEmpty());
    }
}