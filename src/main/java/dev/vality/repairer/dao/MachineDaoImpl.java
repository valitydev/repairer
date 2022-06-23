package dev.vality.repairer.dao;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.repairer.SearchRequest;
import dev.vality.repairer.StatusHistory;
import dev.vality.repairer.constant.SearchConstant;
import dev.vality.repairer.dao.field.ConditionParameterSource;
import dev.vality.repairer.dao.rowmapper.MachineRowMapper;
import dev.vality.repairer.dao.rowmapper.StatusHistoryRowMapper;
import dev.vality.repairer.domain.Tables;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.domain.tables.pojos.Machine;
import dev.vality.repairer.service.TimeHolder;
import dev.vality.repairer.service.TokenGenService;
import org.jooq.Operator;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.jooq.Comparator.EQUALS;

@Component
public class MachineDaoImpl extends AbstractDao implements MachineDao {

    private final TokenGenService tokenGenService;
    private final MachineRowMapper machineRowMapper;
    private final StatusHistoryRowMapper statusHistoryRowMapper;

    public MachineDaoImpl(DataSource dataSource,
                          TokenGenService tokenGenService,
                          MachineRowMapper machineRowMapper,
                          StatusHistoryRowMapper statusHistoryRowMapper) {
        super(dataSource);
        this.tokenGenService = tokenGenService;
        this.machineRowMapper = machineRowMapper;
        this.statusHistoryRowMapper = statusHistoryRowMapper;
    }

    @Override
    public Optional<Long> save(Machine machine) {
        Query query = getDslContext().insertInto(Tables.MACHINE)
                .set(getDslContext().newRecord(Tables.MACHINE, machine))
                .onConflict(Tables.MACHINE.MACHINE_ID,
                        Tables.MACHINE.NAMESPACE,
                        Tables.MACHINE.STATUS,
                        Tables.MACHINE.CREATED_AT)
                .doNothing()
                .returning(Tables.MACHINE.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public void updateCurrent(Machine machine, Long id) {
        Query query = getDslContext().update(Tables.MACHINE)
                .set(Tables.MACHINE.CURRENT, false)
                .where(Tables.MACHINE.MACHINE_ID.eq(machine.getMachineId())
                        .and(Tables.MACHINE.NAMESPACE.eq(machine.getNamespace())
                                .and(Tables.MACHINE.CURRENT.eq(true)
                                        .and(Tables.MACHINE.ID.notEqual(machine.getId())))));
        executeOne(query);
    }

    @Override
    public List<dev.vality.repairer.Machine> search(SearchRequest request) {
        TimeHolder timeHolder = buildTimeHolder(request);
        Query query = getDslContext().selectFrom(Tables.MACHINE)
                .where(appendDateTimeRangeConditions(
                        appendConditions(
                                DSL.trueCondition(),
                                Operator.AND,
                                prepareCondition(request, timeHolder)),
                        Tables.MACHINE.CREATED_AT,
                        timeHolder.getFromTime(),
                        timeHolder.getToTime()
                ))
                .orderBy(Tables.MACHINE.CREATED_AT.desc())
                .limit(request.isSetLimit() ? request.getLimit() : SearchConstant.LIMIT);
        List<dev.vality.repairer.Machine> result = fetch(query, machineRowMapper);
        result.forEach(m -> m.setHistory(getHistory(m)));
        return result;
    }

    private List<StatusHistory> getHistory(dev.vality.repairer.Machine machine) {
        Query query = getDslContext().selectFrom(Tables.MACHINE)
                .where(Tables.MACHINE.MACHINE_ID.eq(machine.getId())
                        .and(Tables.MACHINE.NAMESPACE.eq(machine.getNs())
                                .and(Tables.MACHINE.CURRENT.eq(false))));
        return fetch(query, statusHistoryRowMapper);
    }

    private TimeHolder buildTimeHolder(SearchRequest request) {
        return TimeHolder.builder()
                .fromTime(TypeUtil.stringToLocalDateTime(request.getTimespan().getFromTime()))
                .toTime(TypeUtil.stringToLocalDateTime(request.getTimespan().getToTime()))
                .whereTime(tokenGenService.extractTime(request.getContinuationToken()))
                .build();
    }

    private ConditionParameterSource prepareCondition(SearchRequest searchQuery, TimeHolder timeHolder) {
        return new ConditionParameterSource()
                .addInConditionValue(Tables.MACHINE.MACHINE_ID, searchQuery.getId())
                .addValue(Tables.MACHINE.NAMESPACE, searchQuery.getNs(), EQUALS)
                .addValue(Tables.MACHINE.PROVIDER_ID, searchQuery.getProviderId(), EQUALS)
                .addValue(Tables.MACHINE.ERROR_MESSAGE, searchQuery.getErrorMessage(), EQUALS)
                .addValue(Tables.MACHINE.CURRENT, true, EQUALS)
                .addValue(Tables.MACHINE.STATUS,
                        searchQuery.isSetStatus()
                                ? TypeUtil.toEnumField(searchQuery.getStatus().name(), Status.class)
                                : null,
                        EQUALS);
    }
}
