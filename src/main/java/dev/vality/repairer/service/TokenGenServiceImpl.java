package dev.vality.repairer.service;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.serializer.Geck;
import dev.vality.repairer.Machine;
import dev.vality.repairer.SearchRequest;
import dev.vality.repairer.config.properties.TokenGenProperties;
import dev.vality.repairer.exception.BadTokenException;
import dev.vality.repairer.util.HmacUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenGenServiceImpl implements TokenGenService {

    private final TokenGenProperties tokenGenProperties;

    @Override
    public LocalDateTime extractTime(String token) {
        TokenHolder tokenHolder = extractToken(token);
        return tokenHolder != null ? tokenHolder.getTimestamp() : null;
    }

    @Override
    public void validateToken(SearchRequest query, String validateToken) {
        TokenHolder validateTokenHolder = extractToken(validateToken);
        if (validateTokenHolder != null) {
            LocalDateTime createdAt = validateTokenHolder.getTimestamp();
            String generatedToken = generateToken(query, createdAt);
            TokenHolder generatedTokenHolder = extractToken(generatedToken);
            if (generatedTokenHolder != null
                    && generatedTokenHolder.getToken() != null
                    && validateTokenHolder.getToken() != null
                    && !generatedTokenHolder.getToken().equals(validateTokenHolder.getToken())) {
                throw new BadTokenException("Token validation failure");
            }
        }
    }

    @Override
    public String generateToken(
            SearchRequest searchRequest,
            List<Machine> machines) {
        if (!CollectionUtils.isEmpty(machines) && searchRequest.isSetLimit()
                && machines.size() == searchRequest.getLimit()) {
            var createdAt = machines.get(machines.size() - 1).getCreatedAt();
            return generateToken(searchRequest, TypeUtil.stringToLocalDateTime(createdAt));
        }
        return null;
    }

    @SneakyThrows
    private String generateToken(TBase query, LocalDateTime createdAt) {
        String val = Geck.toJson(query);
        String token = String.format("%s;%s",
                HmacUtil.encode(tokenGenProperties.getKey(), val.getBytes(StandardCharsets.UTF_8)),
                createdAt.atZone(ZoneOffset.UTC).toInstant().toString()
        );
        log.debug("Generated token: {}", token);
        return token;
    }

    private TokenHolder extractToken(String token) {
        if (token == null) {
            return null;
        }
        String[] tokenSplit = token.split(";");
        if (tokenSplit.length != 2) {
            throw new BadTokenException("Bad token format: " + token);
        }
        LocalDateTime timestamp = null;
        try {
            timestamp = TypeUtil.stringToLocalDateTime(tokenSplit[1]);
        } catch (IllegalArgumentException e) {
            log.error("Exception while extract dateTime from: " + token, e);
        }
        return new TokenHolder(tokenSplit[0], timestamp);
    }

    @Data
    private static final class TokenHolder {
        private final String token;
        private final LocalDateTime timestamp;
    }
}
