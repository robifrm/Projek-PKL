package com.vnet.vnet_backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.vnet.vnet_backend.entity.InternetPackage;
import com.vnet.vnet_backend.repository.InternetPackageRepository;
import com.vnet.vnet_backend.repository.CustomerRepository;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final InternetPackageRepository packageRepository;
    private final CustomerRepository customerRepository;

    @GetMapping
    public List<InternetPackage> getAll() {
        return packageRepository.findAll();
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