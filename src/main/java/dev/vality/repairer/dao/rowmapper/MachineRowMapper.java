package dev.vality.repairer.dao.rowmapper;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.repairer.Machine;
import dev.vality.repairer.domain.Tables;
import dev.vality.repairer.domain.enums.Status;
import dev.vality.repairer.util.MapperUtil;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class MachineRowMapper implements RowMapper<Machine> {

    @Override
    public Machine mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Machine()
                .setId(rs.getString(Tables.MACHINE.MACHINE_ID.getName()))
                .setNs(rs.getString(Tables.MACHINE.NAMESPACE.getName()))
                .setCreatedAt(TypeUtil.temporalToString(
                        rs.getObject(Tables.MACHINE.CREATED_AT.getName(), LocalDateTime.class)))
                .setStatus(MapperUtil.map(TypeUtil.toEnumField(
                        rs.getString(Tables.MACHINE.STATUS.getName()), Status.class)))
                .setNs(rs.getString(Tables.MACHINE.NAMESPACE.getName()))
                .setProviderId(rs.getString(Tables.MACHINE.PROVIDER_ID.getName()))
                .setErrorMessage(rs.getString(Tables.MACHINE.ERROR_MESSAGE.getName()));
    }
}
