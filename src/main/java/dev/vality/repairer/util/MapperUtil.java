package dev.vality.repairer.util;

import dev.vality.repairer.RepairStatus;
import dev.vality.repairer.domain.enums.Status;

public class MapperUtil {

    public static RepairStatus map(Status status) {
        return switch (status) {
            case failed -> RepairStatus.failed;
            case repaired -> RepairStatus.repaired;
        };
    }
}
