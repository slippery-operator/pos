package com.increff.pos.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong from backend";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String msg) {
        return "You said: " + msg;
    }
}
