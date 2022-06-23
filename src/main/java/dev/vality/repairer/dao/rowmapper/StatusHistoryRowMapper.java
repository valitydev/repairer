package dev.vality.repairer.dao.rowmapper;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.repairer.StatusHistory;
import dev.vality.repairer.domain.Tables;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.util.MapperUtil;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class StatusHistoryRowMapper implements RowMapper<StatusHistory> {

    @Override
    public StatusHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new StatusHistory()
                .setChangedAt(TypeUtil.temporalToString(
                        rs.getObject(Tables.MACHINE.CREATED_AT.getName(), LocalDateTime.class)))
                .setStatus(MapperUtil.map(TypeUtil.toEnumField(
                        rs.getString(Tables.MACHINE.STATUS.getName()), Status.class)));
    }
}
