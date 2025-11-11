package org.openpodcastapi.opa.advice;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@Log4j2
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @ModelAttribute
    public void addAuthenticationFlag(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
        model.addAttribute("isAuthenticated", isAuthenticated);
    }

    @ModelAttribute
    public void addUserDetails(Principal principal, Model model) {
        String username = principal != null ? principal.getName() : "Guest";
        model.addAttribute("username", username);
    }
}