package org.openpodcastapi.opa.advice;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/// A helper class for adding user information to requests.
///
/// This class is used to populate user details in templates
/// and to ensure that a user is authenticated when viewing
/// web pages.
@Log4j2
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    /// Adds a boolean `isAuthenticated` property to the request model based on
    /// whether the user is logged-in.
    ///
    /// @param model the [Model] attached to the request
    @ModelAttribute
    public void addAuthenticationFlag(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
        model.addAttribute("isAuthenticated", isAuthenticated);
    }

    /// Adds user details to the request model.
    ///
    /// @param principal the [Principal] representing the user
    /// @param model     the [Model] attached to the request
    @ModelAttribute
    public void addUserDetails(Principal principal, Model model) {
        var username = principal != null ? principal.getName() : "Guest";
        model.addAttribute("username", username);
    }
}