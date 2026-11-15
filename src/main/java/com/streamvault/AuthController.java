package com.streamvault;

import com.streamvault.User;
import com.streamvault.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/auth/login")
    public String loginUser(@RequestParam("loginId") String loginId,
                            @RequestParam("password") String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticate(loginId, password);
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/auth/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("birthdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
                               @RequestParam("phoneNumber") String phoneNumber,
                               @RequestParam("password") String password,
                               RedirectAttributes redirectAttributes) {
        try {
            // Saves user credentials securely in the database
            userService.registerUser(username, email, birthdate, phoneNumber, password);

            redirectAttributes.addFlashAttribute("successMsg", "Account created successfully! Please sign in with your credentials.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/contact")
    public String showContactPage() {
        return "contact";
    }

    @PostMapping("/auth/contact")
    public String submitContactUs(@RequestParam("fullName") String fullName,
                                  @RequestParam("email") String email,
                                  @RequestParam("subject") String subject,
                                  @RequestParam("message") String message,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.saveContactMessage(fullName, email, subject, message);
            redirectAttributes.addFlashAttribute("successMsg", "Thank you! Your message has been sent successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Could not send message. Please verify all fields.");
        }
        return "redirect:/contact";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMsg", "You have signed out safely.");
        return "redirect:/login";
    }
}