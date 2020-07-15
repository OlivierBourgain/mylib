package com.obourgain.mylib.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.obourgain.mylib.service.UserService;
import com.obourgain.mylib.vobj.User.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

public class AbstractResource {
    private static Logger log = LoggerFactory.getLogger(AbstractResource.class);

    @Autowired
    private UserService userService;

    private static JsonFactory jsonFactory = new JacksonFactory();
    private static String CLIENT_ID = "274726541955-21jeen018spaeumspmifv18hgomju0r9.apps.googleusercontent.com";
    private static String BEARER = "Bearer ";

    /**
     * Checks the user is authentified and has either `USER` or `ADMIN` role.
     *
     * @return UserId
     * @throws AccessDeniedException is the user is not authenticated, or doesn't have any role.
     */
    public String checkAccess(HttpServletRequest req) throws Exception {
        return checkAccess(req, List.of(UserRole.ADMIN, UserRole.USER));
    }

    /**
     * Checks the user is authentified and has a given role.
     *
     * @return UserId
     * @throws AccessDeniedException is the user is not authenticated, or doesn't have the requested role.
     */
    public String checkAccess(HttpServletRequest req, UserRole role) throws Exception {
        return checkAccess(req, List.of(role));
    }

    /**
     * Retrieve the clientId from the Google Auth token.
     */
    private String checkAccess(HttpServletRequest req, List<UserRole> roles) throws Exception {

        String tokenStr = req.getHeader("Authorization");
        log.debug("Header in request " + tokenStr);

        if (StringUtils.isBlank(tokenStr) || !tokenStr.startsWith(BEARER)) {
            throw new AccessDeniedException("No bearer token");
        }

        String token = tokenStr.substring(BEARER.length());
        log.debug("token " + token);

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        // (Receive idTokenString by HTTPS POST)
        GoogleIdToken idToken = verifier.verify(token);
        if (idToken == null) throw new AccessDeniedException("Invalid token");

        // Checking role
        GoogleIdToken.Payload payload = idToken.getPayload();
        userService.getByEmail(payload.getEmail())
                .filter(u -> roles.contains(u.getRole()))
                .orElseThrow(() -> new AccessDeniedException("User not authorized"));
        return payload.getSubject();

    }


}
