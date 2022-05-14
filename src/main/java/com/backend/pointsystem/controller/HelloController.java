package com.backend.pointsystem.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/hello")
@RestController
public class HelloController {
    @PostMapping
    public HelloDto hello(@RequestParam String message) {
        return new HelloDto(message);
    }
}
