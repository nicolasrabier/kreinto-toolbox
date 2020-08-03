package com.kreinto.toolbox.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainController {

    @RequestMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("homepage.html");
        return modelAndView;
    }

    /*
    @RequestMapping("/hello")
    public String hello() {
        return "oi oi!";
    }
    */
}
