package org.openpodcastapi.opa.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.user.UserDTO;
import org.openpodcastapi.opa.user.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j2
@RequiredArgsConstructor
public class UiAuthController {
    private static final String USER_REQUEST_ATTRIBUTE = "createUserRequest";
    private static final String REGISTER_TEMPLATE = "auth/register";
    private final UserService userService;

    // === Login page ===
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "auth/login";
    }

    // === Logout confirmation page ===
    @GetMapping("/logout-confirm")
    public String logoutPage() {
        return "auth/logout";
    }

    // === Registration page ===
    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute(USER_REQUEST_ATTRIBUTE, new UserDTO.CreateUserDTO("", "", ""));
        return REGISTER_TEMPLATE;
    }

    // === Registration POST handler ===
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute UserDTO.CreateUserDTO createUserRequest,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute(USER_REQUEST_ATTRIBUTE, createUserRequest);
            return REGISTER_TEMPLATE;
        }

        try {
            userService.createAndPersistUser(createUserRequest);
        } catch (DataIntegrityViolationException _) {
            result.rejectValue("username", "", "Username or email already exists");
            model.addAttribute(USER_REQUEST_ATTRIBUTE, createUserRequest);
            return REGISTER_TEMPLATE;
        }

        return "redirect:/login?registered";
    }
}
