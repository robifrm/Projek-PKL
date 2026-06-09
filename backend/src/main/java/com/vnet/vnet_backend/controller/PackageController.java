package com.vnet.vnet_backend.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

import com.vnet.vnet_backend.entity.InternetPackage;
import com.vnet.vnet_backend.repository.InternetPackageRepository;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final InternetPackageRepository packageRepository;

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
        packageRepository.deleteById(id);
    }
}