package com.enrollment.Bolbolestan.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWT {
    private static final String issuer = "https://bolbolestan.enrollment.com";

    private static final String SECRET_KEY = "bolbolestan";

    public static String createToken(String id, Integer expireTime) throws UnsupportedEncodingException {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long ttlMillis = TimeUnit.MINUTES.toMillis(expireTime);
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode header = mapper.createObjectNode();
        header.put("alg", "HMACSHA256");
        header.put("typ", "JWT");
        ObjectNode payload = mapper.createObjectNode();
        payload.put("iat", now.getTime() / 1000);
        payload.put("iss", issuer);
        payload.put("exp", exp.getTime() / 1000);
        payload.put("userId", id);
        String encodedHeader = encode(header.toString().getBytes());
        String encodedPayload = encode(payload.toString().getBytes());
        String signature = hmacSha256(encodedHeader + "." + encodedPayload, SECRET_KEY);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public static JsonNode decodeToken(String token) throws Exception {
        String[] chunks = token.split("\\.");

        String payload = decode(chunks[1]);
        String signature = chunks[2];

        boolean isValid = signature.equals(hmacSha256(chunks[0] + "." + chunks[1], SECRET_KEY));

        if (!isValid) {
            throw new Exception("Could not verify JWT token integrity!");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode decodedToken = mapper.readTree(payload);
        return decodedToken;
    }

    private static String hmacSha256(String data, String secret) {
        try {
            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(JWT.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

}

