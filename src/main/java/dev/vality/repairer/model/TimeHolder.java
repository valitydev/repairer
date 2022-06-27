package dev.vality.repairer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TimeHolder {

    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private LocalDateTime whereTime;

}
