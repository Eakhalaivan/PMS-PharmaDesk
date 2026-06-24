package com.pharmadesk.backend.security;

import com.pharmadesk.backend.model.User;
import com.pharmadesk.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        boolean isEnabled = !"SUSPENDED".equals(user.getStatus());

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (com.pharmadesk.backend.model.Role role : user.getRoles()) {
            String roleName = role.getName();
            authorities.add(new SimpleGrantedAuthority(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName));
            
            String upper = roleName.toUpperCase();
            if (upper.contains("ADMIN")) authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));
            else if (upper.contains("PHARMAC")) authorities.add(new SimpleGrantedAuthority("ROLE_PHARMACY_STAFF"));
            else if (upper.contains("BILL") || upper.contains("ACCOUNT") || upper.contains("CASH")) authorities.add(new SimpleGrantedAuthority("ROLE_BILLING_STAFF"));
            else if (upper.contains("STORE") || upper.contains("INVENT") || upper.contains("PURCHASE")) authorities.add(new SimpleGrantedAuthority("ROLE_STOREKEEPER"));
            else if (upper.contains("LAB") || upper.contains("PATHOLOG")) authorities.add(new SimpleGrantedAuthority("ROLE_LAB_TECHNICIAN"));
            else if (upper.contains("SUPERVISOR") || upper.contains("MANAGER")) authorities.add(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));
            else if (upper.contains("RECEPTION") || upper.contains("FRONT")) authorities.add(new SimpleGrantedAuthority("ROLE_RECEPTIONIST"));
            else if (upper.contains("AUDIT") || upper.contains("COMPLIANCE")) authorities.add(new SimpleGrantedAuthority("ROLE_AUDIT_COMPLIANCE"));
            else if (upper.contains("SENIOR") && upper.contains("MEDIC")) authorities.add(new SimpleGrantedAuthority("ROLE_SENIOR_MEDICAL_STAFF"));
            else if (upper.contains("MEDIC") || upper.contains("DOCTOR") || upper.contains("PHYSICIAN") || upper.contains("NURS")) authorities.add(new SimpleGrantedAuthority("ROLE_MEDICAL_STAFF"));
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PHARMACY_STAFF"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPasswordHash(),
                isEnabled,   // enabled
                true,        // accountNonExpired
                true,        // credentialsNonExpired
                isEnabled,   // accountNonLocked
                authorities
        );
    }
}
