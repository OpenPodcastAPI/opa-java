package org.openpodcastapi.opa.controllers.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/// Controller for the home and landing page controllers
@Controller
public class HomeController {

    /// Controller for the landing page.
    ///
    /// @return the landing page
    @GetMapping("/")
    public String getLandingPage() {
        return "landing";
    }

    /// Controller for an authenticated user's homepage.
    /// Redirects users to the login page if they're not authenticated.
    ///
    /// @param auth the authentication object for the user
    /// @return the home page
    @GetMapping("/home")
    public String getHomePage(Authentication auth) {
        if (auth != null && !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        return "home";
    }
}
