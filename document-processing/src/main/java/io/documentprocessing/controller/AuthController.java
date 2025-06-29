package io.documentprocessing.controller;

import io.documentprocessing.model.User;
import io.documentprocessing.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UserRepository userRepository;
	private static final String CORS_ORIGIN_URL = "http://localhost:5173";

	
	public AuthController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String email = userData.get("email");
        String password = userData.get("password");

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body("All fields are required");
        }

        if (userRepository.findByUsername(username) != null) {
            return ResponseEntity.status(409).body("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email); 
        user.setPassword(password); 

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "userId", user.getId(),
                "username", user.getUsername()
        ));
    }
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
	    String username = loginData.get("username");
	    String password = loginData.get("password");

	    User user = userRepository.findByUsername(username);

	    if (user != null && user.getPassword().equals(password)) {

	    	return ResponseEntity.ok(Map.of(
	            "userId", user.getId(),
	            "username", user.getUsername()
	        ));
	    } else {
	        return ResponseEntity.status(401).body("Invalid username or password");
	    }
	}
	

	@GetMapping("/auth/status") 
	public ResponseEntity<?> checkLoginStatus(HttpServletRequest request) { 
		HttpSession session = request.getSession(false); 
		if (session != null && session.getAttribute("user") != null) 
		{ return ResponseEntity.ok(session.getAttribute("user")); } return
					ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in"); 
	}
	 



}
