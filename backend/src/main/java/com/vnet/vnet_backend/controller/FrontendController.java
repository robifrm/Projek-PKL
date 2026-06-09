package com.vnet.vnet_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({
            "/",
            "/dashboard",
            "/data-import",
            "/data-preview",
            "/customers",
            "/agent-performance",
            "/address-insights",
            "/settings",
            "/support"
    })
    public String index() {
        return "forward:/index.html";
    }
}
