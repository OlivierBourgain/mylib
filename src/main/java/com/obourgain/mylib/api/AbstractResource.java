package com.obourgain.mylib.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;

public class AbstractResource {
    private static Logger log = LoggerFactory.getLogger(AbstractResource.class);

    private static JsonFactory jsonFactory = new JacksonFactory();
    private static String CLIENT_ID = "274726541955-21jeen018spaeumspmifv18hgomju0r9.apps.googleusercontent.com";
    private static String BEARER = "Bearer ";

    /**
     * Retrieve the clientId from the Google Auth token.
     */
    public Optional<String> getClient(HttpServletRequest req) throws Exception {

        String tokenStr = req.getHeader("Authorization");
        log.debug("Header in request " + tokenStr);

        if (StringUtils.isBlank(tokenStr) || !tokenStr.startsWith(BEARER)) {
            return Optional.empty();
        }

        String token = tokenStr.substring(BEARER.length());
        log.debug("token " + token);

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        // (Receive idTokenString by HTTPS POST)
        GoogleIdToken idToken = verifier.verify(token);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            log.info("Google token verified");
            // Print user identifier
            return Optional.of(payload.getSubject());
        }
        return Optional.empty();
    }
}
