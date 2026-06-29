package com.pharmadesk.backend.controller;

import com.pharmadesk.backend.model.Role;
import com.pharmadesk.backend.model.User;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.repository.RoleRepository;
import com.pharmadesk.backend.repository.UserRepository;
import com.pharmadesk.backend.security.JwtUtils;
import com.pharmadesk.backend.dto.LoginRequest;
import com.pharmadesk.backend.dto.CreateUserRequest;
import com.pharmadesk.backend.dto.UserRequestDto;
import com.pharmadesk.backend.dto.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtils jwtUtils, 
                          UserRepository userRepository, 
                          RoleRepository roleRepository, 
                          PasswordEncoder passwordEncoder,
                          com.pharmadesk.backend.service.OtpService otpService,
                          RedisTemplate<String, Object> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.redisTemplate = redisTemplate;
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .maxAge(3600)
                    .path("/")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            List<String> roleNames = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("id",                user.getId());
            data.put("name",              user.getName());
            data.put("username",          user.getUsername());
            data.put("email",             user.getEmail());
            data.put("branch",            user.getBranch());
            data.put("roles",             roleNames);
            data.put("mustChangePassword", user.isMustChangePassword());
            data.put("token",             jwt);

            return ResponseEntity.ok(ApiResponse.success(data, "Login successful"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid username or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String token = getJwtFromCookies(request);
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String jti = jwtUtils.getJtiFromJwtToken(token);
            if (Boolean.TRUE.equals(redisTemplate.hasKey("jwt_blacklist:" + jti))) {
                return ResponseEntity.status(401).body(ApiResponse.error("Token has been revoked"));
            }
            
            String username = jwtUtils.getUserNameFromJwtToken(token);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                // Generate new token
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, null, jwtUtils.getAuthoritiesFromJwtToken(token));
                String newJwt = jwtUtils.generateJwtToken(authentication);
                
                ResponseCookie cookie = ResponseCookie.from("jwt", newJwt)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Strict")
                        .maxAge(3600)
                        .path("/")
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                return ResponseEntity.ok(ApiResponse.success(null, "Token refreshed"));
            }
        }
        return ResponseEntity.status(401).body(ApiResponse.error("Invalid or expired token"));
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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
    @Transactional
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody CreateUserRequest userDto) {
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
        user.setMustChangePassword(true); // Force password change on first login

        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            Set<Role> roles = userDto.getRoles().stream()
                    .map(name -> roleRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException(
                                    "Role not found: " + name + ". Valid roles: " +
                                    roleRepository.findAll().stream()
                                            .map(Role::getName)
                                            .collect(Collectors.joining(", "))
                            )))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User savedFirst = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserResponseDTO.from(savedFirst), "Staff created"));
    }

    @PutMapping("/users/{id}/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getUsername().equals(principal.getName())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied: You can only update your own profile"));
        }

        if (request.containsKey("name")) user.setName(request.get("name"));
        if (request.containsKey("email")) user.setEmail(request.get("email"));
        if (request.containsKey("phone")) user.setPhone(request.get("phone"));
        if (request.containsKey("branch")) user.setBranch(request.get("branch"));
        if (request.containsKey("location")) user.setBranch(request.get("location"));
        if (request.containsKey("shift")) user.setShift(request.get("shift"));

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserResponseDTO.from(savedUser), "Profile updated successfully"));
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
                            .orElseThrow(() -> new RuntimeException(
                                    "Role not found: " + name + ". Valid roles: " +
                                    roleRepository.findAll().stream()
                                            .map(Role::getName)
                                            .collect(Collectors.joining(", "))
                            )))
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
    public ResponseEntity<ApiResponse<Void>> logoutUser(HttpServletRequest request, HttpServletResponse response, Principal principal) {
        String token = getJwtFromCookies(request);
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String jti = jwtUtils.getJtiFromJwtToken(token);
            Date expiration = jwtUtils.getExpirationFromJwtToken(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set("jwt_blacklist:" + jti, "true", ttl, TimeUnit.MILLISECONDS);
            }
        }
        
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

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
