package com.library.management.controller;

import com.library.management.model.User;
import com.library.management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepo;
    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; 
    }
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) { 
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return "redirect:/auth/login?error=invalid_credentials";
        }
        session.setAttribute("currentUser", user);
        return "redirect:/";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/auth/login"; 
    }
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; 
    }
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String email) {    
        if (userRepo.findByUsername(username).isPresent()) {
            return "redirect:/auth/login?error=username_exists";
        }
        User user = new User(username, password, fullName, email, "READER");
        userRepo.save(user);
        return "redirect:/auth/login?success=registered";
    }
}