package com.kreinto.toolbox.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor

public class User {

    private String username;
    private String password;
    private String fullname;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date birtdate;
}
