package user_service.user_service.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.function.Supplier;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import user_service.user_service.entity.User;
import user_service.user_service.security.JwtUtil;
import user_service.user_service.service.UserService;

@SecurityRequirement(name = "bearerAuth")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
public class UserController {
	
	@Autowired
	private UserService uService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user)
	{
		Optional<User> existingUser=uService.findByEmail(user.getEmail());
		if(existingUser.isPresent())
		{
			return ResponseEntity.badRequest().body("Email is already exists");
		}
		return ResponseEntity.ok(uService.register(user));
	}
	@GetMapping("/{id}")
	public ResponseEntity<?> findUserById(@PathVariable Long id) {
	    Optional<User> userOpt = uService.findUserById(id);
	    
	    if (userOpt.isPresent()) {
	        return ResponseEntity.ok(userOpt.get());
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                             .body("User not found with ID: " + id);
	    }
	}

	
	@GetMapping("/email/{email}")
	public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
	    Optional<User> user = uService.findByEmail(email);
	    return user.map(ResponseEntity::ok)
	               .orElse(ResponseEntity.status(404)
	            		   .header("X-Error", "User not found")
	            		   .body(null));
	}

	@GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(uService.getAllUsers());
    }
	
	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
	    Optional<User> user = uService.findByEmail(loginRequest.getEmail());

	    if (user.isEmpty() || !user.get().getPassword().equals(loginRequest.getPassword())) {
	        return ResponseEntity.status(401).body("Invalid email or password");
	    }

	    String token = jwtUtil.generateToken(user.get().getEmail());

	    return ResponseEntity.ok(Collections.singletonMap("token", token));
	}



}
