package dev.vality.repairer.service;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TimeHolder {

    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private LocalDateTime whereTime;

}
