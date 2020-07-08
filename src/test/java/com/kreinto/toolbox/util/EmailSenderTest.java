package com.kreinto.toolbox.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailSenderTest {

    @Test
    public void SendSimpleEmail(){
        EmailSender.sendPlainEmail("nicolas.rabier@gmail.com", null, "Test from Java", "This is the content of the email. Bye");
    }

}