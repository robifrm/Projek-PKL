package com.vnet.vnet_backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

import com.vnet.vnet_backend.entity.InternetPackage;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.InternetPackageRepository;
import com.vnet.vnet_backend.repository.CustomerRepository;
import com.vnet.vnet_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final InternetPackageRepository packageRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<InternetPackage> getAll() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> currentUserOpt = userRepository.findByUsernameIgnoreCase(currentUsername);
        User currentUser = currentUserOpt.orElse(null);

        if (currentUser != null && currentUser.getRole() == Role.AGENT && currentUser.getAgent() != null) {
            return packageRepository.findPackagesSoldByAgentId(currentUser.getAgent().getId());
        }
        return packageRepository.findAll();
    }

    @GetMapping("/public")
    public List<InternetPackage> getAllPublic() {
        return packageRepository.findAll();
    }

    @GetMapping("/{id}/public")
    public ResponseEntity<InternetPackage> getByIdPublic(@PathVariable Long id) {
        return packageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public InternetPackage create(@RequestBody InternetPackage pkg) {
        return packageRepository.save(pkg);
    }

    @PutMapping("/{id}")
    public InternetPackage update(@PathVariable Long id, @RequestBody InternetPackage pkg) {
        pkg.setId(id);
        return packageRepository.save(pkg);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!packageRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paket tidak ditemukan");
        }
        if (customerRepository.existsByPkgId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paket tidak dapat dihapus karena sedang digunakan oleh pelanggan");
        }
        packageRepository.deleteById(id);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getReason());
        body.put("message", ex.getReason());
        body.put("status", ex.getStatusCode().value());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}