package dev.vality.repairer.dao;

import dev.vality.repairer.SearchRequest;
import dev.vality.repairer.domain.tables.pojos.Machine;
import dev.vality.repairer.config.PostgresqlSpringBootITest;
import dev.vality.repairer.util.MapperUtil;
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
        assertFalse(result.isEmpty());
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
    public void search() {
        Machine source = random(Machine.class, "id", "current");
        machineDao.save(source);
        SearchRequest request = new SearchRequest()
                .setId(List.of(source.getMachineId()))
                .setNs(source.getNamespace())
                .setStatus(MapperUtil.map(source.getStatus()))
                .setProviderId(source.getProviderId())
                .setErrorMessage(source.getErrorMessage());
        var result = machineDao.search(request);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(source.getMachineId(), result.get(0).getId());
    }
}