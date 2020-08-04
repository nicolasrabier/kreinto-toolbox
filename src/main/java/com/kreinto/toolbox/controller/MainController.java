package com.kreinto.toolbox.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainController {

    @RequestMapping("/")
    public ModelAndView index() {
        return new ModelAndView("homepage.html");
    }

    //@RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    @RequestMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login.html");
    }
}
