package com.obourgain.mylib.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class AccountRessource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(AccountRessource.class);

    @RequestMapping(value="/csrf-token", method= RequestMethod.GET)
    public @ResponseBody
    String getCsrfToken(HttpServletRequest request) throws Exception {
        log.info("Getting csrf token");
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        CsrfToken token = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
        log.info(token.toString());
        return token.getToken();
    }
}
