package dev.vality.repairer.service;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.repairer.Machine;
import dev.vality.repairer.SearchRequest;
import dev.vality.repairer.config.properties.TokenGenProperties;
import dev.vality.testcontainers.annotations.util.RandomBeans;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenGenServiceTest {

    @Test
    public void test() {
        TokenGenProperties tokenGenProperties = new TokenGenProperties();
        tokenGenProperties.setKey("kek");
        TokenGenService tokenGenService = new TokenGenServiceImpl(tokenGenProperties);
        SearchRequest searchRequest = RandomBeans.randomThrift(SearchRequest.class);
        searchRequest.setContinuationToken(null);
        searchRequest.setLimit(1);
        Machine machine = RandomBeans.randomThrift(Machine.class);
        String token = tokenGenService.generateToken(searchRequest, List.of(machine));
        tokenGenService.validateToken(searchRequest, token);
        LocalDateTime extractTime = tokenGenService.extractTime(token);
        assertEquals(TypeUtil.stringToLocalDateTime(machine.getCreatedAt()), extractTime);
    }
}