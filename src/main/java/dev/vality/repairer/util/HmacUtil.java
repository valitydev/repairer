package dev.vality.repairer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HmacUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String encode(String key, byte[] data) throws GeneralSecurityException {
        if (key == null || data == null) {
            throw new IllegalArgumentException("key/data can't be null");
        }

        final Mac hmac = Mac.getInstance(HMAC_SHA256);
        byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
        final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, HMAC_SHA256);
        hmac.init(secretKey);
        byte[] res = hmac.doFinal(data);

        return Base64.encodeBase64URLSafeString(res);
    }

}
