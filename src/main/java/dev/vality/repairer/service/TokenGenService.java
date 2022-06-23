package dev.vality.repairer.service;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.serializer.Geck;
import dev.vality.repairer.SearchRequest;
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
import java.util.function.Function;


@Slf4j
@Service
@RequiredArgsConstructor
public class TokenGenService {

    @Value("${service.token-gen.key}")
    private String key;

    public LocalDateTime extractTime(String token) {
        TokenHolder tokenHolder = extractToken(token);
        return tokenHolder != null ? tokenHolder.getTimestamp() : null;
    }

    public void validateToken(TBase query, String validateToken) {
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

    public <T> String generateToken(
            TBase query,
            SearchRequest commonParams,
            List<T> objects,
            Function<List<T>, T> lastElementFunction,
            Function<T, String> dateTimeFunction) {
        return generateToken(query, commonParams, objects, dateTimeFunction.compose(lastElementFunction));
    }

    public <T> String generateToken(
            TBase query,
            SearchRequest commonParams,
            List<T> objects,
            Function<List<T>, String> dateTimeFunction) {
        if (!CollectionUtils.isEmpty(objects) && commonParams.isSetLimit()
                && objects.size() == commonParams.getLimit()) {
            final String createdAt = dateTimeFunction.apply(objects);
            return generateToken(query, TypeUtil.stringToLocalDateTime(createdAt));
        }
        return null;
    }

    @SneakyThrows
    private String generateToken(TBase query, LocalDateTime createdAt) {
        String val = Geck.toJson(query);
        String token = String.format("%s;%s",
                HmacUtil.encode(key, val.getBytes(StandardCharsets.UTF_8)),
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
