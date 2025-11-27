package org.openpodcastapi.opa.advice;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

import static org.slf4j.LoggerFactory.getLogger;

/// A helper class for adding user information to requests.
///
/// This class is used to populate user details in templates
/// and to ensure that a user is authenticated when viewing
/// web pages.
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    private static final Logger log = getLogger(GlobalModelAttributeAdvice.class);

    /// Adds a boolean `isAuthenticated` property to the request model based on
    /// whether the user is logged-in.
    ///
    /// @param model the variables attached to the request
    @ModelAttribute
    public void addAuthenticationFlag(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final var isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
        assert authentication != null;
        log.debug("Authentication flag for {} added", authentication.getPrincipal());
        model.addAttribute("isAuthenticated", isAuthenticated);
    }

    /// Adds user details to the request model.
    ///
    /// @param principal the principal representing the user
    /// @param model     the variables attached to the request
    @ModelAttribute
    public void addUserDetails(Principal principal, Model model) {
        final var username = principal != null ? principal.getName() : "Guest";
        log.debug("User details for {} added to model", username);
        model.addAttribute("username", username);
    }
}