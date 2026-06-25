package com.pharmadesk.backend.security;

import com.pharmadesk.backend.model.User;
import com.pharmadesk.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + username));

        // Block SUSPENDED and INACTIVE users at the Spring Security layer
        boolean accountEnabled = "ACTIVE".equalsIgnoreCase(user.getStatus());

        List<SimpleGrantedAuthority> authorities = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream()
                    .filter(role -> role != null && role.getName() != null)
                    .map(role -> {
                        String name = role.getName().replace(" ", "_").toUpperCase();
                        if (!name.startsWith("ROLE_")) name = "ROLE_" + name;
                        return new SimpleGrantedAuthority(name);
                    })
                    .collect(Collectors.toList());

        // Users with no roles get NO access (not pharmacy staff access)
        if (authorities.isEmpty()) {
            // Return enabled=false so login is rejected cleanly with "Bad credentials"
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPasswordHash(),
                    false,        // enabled
                    true,         // accountNonExpired
                    true,         // credentialsNonExpired
                    true,         // accountNonLocked
                    List.of(new SimpleGrantedAuthority("ROLE_NO_ACCESS"))
            );
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                accountEnabled,   // enabled — false = login rejected for SUSPENDED/INACTIVE
                true,             // accountNonExpired
                true,             // credentialsNonExpired
                true,             // accountNonLocked
                authorities
        );
    }
}
