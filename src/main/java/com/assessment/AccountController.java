package com.assessment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class AccountController {

    public static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @CrossOrigin
    @PostMapping("/login")
    @ResponseBody
    public Principal user(Principal principal) {

        logger.info("user  : {} ", principal);
        return principal;
    }

    @CrossOrigin
    @GetMapping("/principal")
    @ResponseBody
    public Principal getPrincipal(Principal principal) {
        logger.info("user  : {} ", principal);
        return principal;
    }

}
