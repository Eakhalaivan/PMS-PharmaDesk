package com.pharmadesk.backend.controller;

import com.pharmadesk.backend.model.Role;
import com.pharmadesk.backend.model.User;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.repository.RoleRepository;
import com.pharmadesk.backend.repository.UserRepository;
import com.pharmadesk.backend.security.JwtUtils;
import com.pharmadesk.backend.dto.UserRequestDto;
import com.pharmadesk.backend.dto.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final com.pharmadesk.backend.service.OtpService otpService;

    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtils jwtUtils, 
                          UserRepository userRepository, 
                          RoleRepository roleRepository, 
                          PasswordEncoder passwordEncoder,
                          com.pharmadesk.backend.service.OtpService otpService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
        }
        otpService.sendOtp(email);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully to " + email));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email and code are required"));
        }
        boolean verified = otpService.verifyOtp(email, code);
        if (verified) {
            return ResponseEntity.ok(ApiResponse.success(null, "OTP verified successfully"));
        } else {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid or expired OTP code"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password")));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            User user = userRepository.findByUsername(loginRequest.get("username"))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Track last login timestamp
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            List<String> roleNames = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("token",             jwt);
            data.put("id",                user.getId());
            data.put("name",              user.getName());
            data.put("username",          user.getUsername());
            data.put("email",             user.getEmail());
            data.put("branch",            user.getBranch());
            data.put("roles",             roleNames);
            data.put("mustChangePassword", user.isMustChangePassword());

            return ResponseEntity.ok(ApiResponse.success(data, "Login successful"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid username or password"));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> dtos = userRepository.findAll()
                .stream()
                .map(UserResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, "Users fetched"));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@RequestBody UserRequestDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username already exists"));
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setBranch(userDto.getBranch());
        user.setShift(userDto.getShift());
        user.setStatus("ACTIVE");

        // Initial save to get ID for EMP ID
        User savedFirst = userRepository.save(user);
        savedFirst.setEmployeeId("EMP-" + String.format("%06d", savedFirst.getId()));

        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> roles = userDto.getRoles().stream()
                    .map(name -> roleRepository.findByName(name)
                            .orElseGet(() -> {
                                Role r = new Role();
                                r.setName(name);
                                return roleRepository.save(r);
                            }))
                    .collect(Collectors.toSet());
            savedFirst.setRoles(roles);
        }

        User finalSaved = userRepository.save(savedFirst);
        return ResponseEntity.ok(ApiResponse.success(UserResponseDTO.from(finalSaved), "Staff created"));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@PathVariable Long id, @RequestBody UserRequestDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setBranch(userDto.getBranch());
        user.setShift(userDto.getShift());
        user.setStatus(userDto.getStatus());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        }

        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> roles = userDto.getRoles().stream()
                    .filter(name -> name != null && !name.isBlank())
                    .map(name -> roleRepository.findByName(name)
                            .orElseGet(() -> {
                                Role r = new Role();
                                r.setName(name);
                                return roleRepository.save(r);
                            }))
                    .collect(Collectors.toSet());
            if (!roles.isEmpty()) {
                user.setRoles(roles);
            }
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserResponseDTO.from(savedUser), "Staff updated"));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newStatus = "ACTIVE".equals(user.getStatus()) ? "SUSPENDED" : "ACTIVE";
        user.setStatus(newStatus);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserResponseDTO.from(savedUser),
                "User " + (newStatus.equals("ACTIVE") ? "activated" : "suspended") + " successfully"));
    }

    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetUserPassword(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a secure temporary password
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }
        String rawPassword = "Ph@" + tempPassword;

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setMustChangePassword(true); // Force password change on next login
        userRepository.save(user);

        Map<String, String> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("temporaryPassword", rawPassword);
        result.put("name", user.getName());
        return ResponseEntity.ok(ApiResponse.success(result, "Password reset successfully"));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeleted(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(null, "Staff deleted"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                user.setLastLogout(LocalDateTime.now());
                userRepository.save(user);
            });
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Password must be at least 6 characters"));
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false); // Clear the flag after change
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
