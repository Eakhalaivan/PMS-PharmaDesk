package com.pharmadesk.backend.dto;

import java.util.Set;
import java.util.stream.Collectors;
import com.pharmadesk.backend.model.User;

public class UserResponseDTO {
    public Long id;
    public String username;
    public String name;
    public String email;
    public String phone;
    public String branch;
    public String shift;
    public String status;
    public String employeeId;
    public Set<String> roles;
    // Return as ISO string to avoid Jackson array serialization issue
    public String lastLogin;
    public String lastLogout;
    public boolean mustChangePassword;

    public static UserResponseDTO from(User u) {
        if (u == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.id = u.getId();
        dto.username = u.getUsername();
        dto.name = u.getName();
        dto.email = u.getEmail();
        dto.phone = u.getPhone();
        dto.branch = u.getBranch();
        dto.shift = u.getShift();
        dto.status = u.getStatus();
        dto.employeeId = u.getEmployeeId();
        dto.lastLogin  = u.getLastLogin()  != null ? u.getLastLogin().toString()  : null;
        dto.lastLogout = u.getLastLogout() != null ? u.getLastLogout().toString() : null;
        dto.mustChangePassword = u.isMustChangePassword();
        dto.roles = u.getRoles() == null ? Set.of() :
            u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        return dto;
    }
}
