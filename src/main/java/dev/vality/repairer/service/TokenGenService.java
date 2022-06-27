package dev.vality.repairer.service;

import dev.vality.repairer.Machine;
import dev.vality.repairer.SearchRequest;
import org.apache.thrift.TBase;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenGenService {

    LocalDateTime extractTime(String token);

    void validateToken(SearchRequest query, String validateToken);

    String generateToken(SearchRequest searchRequest, List<Machine> machines);

}
