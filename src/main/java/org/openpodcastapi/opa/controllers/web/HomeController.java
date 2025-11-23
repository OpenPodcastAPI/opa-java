package org.openpodcastapi.opa.controllers.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
public class HomeController {

    // === Landing page ===
    @GetMapping("/")
    public String getLandingPage() {
        return "landing";
    }

    // === Authenticated homepage ===
    @GetMapping("/home")
    public String getHomePage(Authentication auth) {
        if (auth != null && !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        return "home";
    }
}
