package com.pharmadesk.backend.controller;

import com.pharmadesk.backend.model.Role;
import com.pharmadesk.backend.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        if (role.getIsSystemDefault() == null) {
            role.setIsSystemDefault(false);
        }
        return ResponseEntity.ok(roleRepository.save(role));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role updatedRole) {
        return roleRepository.findById(id).map(role -> {
            role.setName(updatedRole.getName());
            role.setColor(updatedRole.getColor());
            role.setPermissionsJson(updatedRole.getPermissionsJson());
            return ResponseEntity.ok(roleRepository.save(role));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        return roleRepository.findById(id).map(role -> {
            if (Boolean.TRUE.equals(role.getIsSystemDefault())) {
                return ResponseEntity.badRequest().<Void>build();
            }
            roleRepository.delete(role);
            return ResponseEntity.ok().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
