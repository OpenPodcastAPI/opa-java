package org.openpodcastapi.opa.controllers.web;

import jakarta.validation.Valid;
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

/// Controller for the web authentication endpoints
@Controller
public class WebAuthController {
    private static final String USER_REQUEST_ATTRIBUTE = "createUserRequest";
    private static final String REGISTER_TEMPLATE = "auth/register";
    private final UserService userService;

    /// Constructor for the web auth controller
    ///
    /// @param userService the [UserService] class to handle user interactions
    public WebAuthController(UserService userService) {
        this.userService = userService;
    }

    /// Controller for the login page.
    /// Displays an error message if a previous login was unsuccessful.
    ///
    /// @param error an optional error message
    /// @param model a placeholder for additional data to be passed to Thymeleaf
    /// @return the login page
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "auth/login";
    }

    /// Controller for the logout confirmation page.
    /// Logouts are handled by Spring Security, this page displays a confirmation only.
    ///
    /// @return the logout confirmation page
    @GetMapping("/logout-confirm")
    public String logoutPage() {
        return "auth/logout";
    }

    /// Controller for the account registration page.
    ///
    /// @param model a placeholder for additional data to be passed to Thymeleaf
    /// @return the account registration page template
    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute(USER_REQUEST_ATTRIBUTE, new UserDTO.CreateUserDTO("", "", ""));
        return REGISTER_TEMPLATE;
    }

    /// Controller for the account registration form.
    ///
    /// @param createUserRequest the [UserDTO.CreateUserDTO] containing the new account details
    /// @param result            the [BindingResult] for displaying data validation errors
    /// @param model             a placeholder for additional data to be passed to Thymeleaf
    /// @return a redirect to the login page, if successful
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
            result.rejectValue("username", "", "Username or email already in use");
            model.addAttribute(USER_REQUEST_ATTRIBUTE, createUserRequest);
            return REGISTER_TEMPLATE;
        }

        return "redirect:/login?registered";
    }
}
