package com.kreinto.toolbox.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @RequestMapping("/")
    public String index() {
        return "You're in mate!";
    }

    /*
    @RequestMapping("/hello")
    public String hello() {
        return "oi oi!";
    }
    */
}
