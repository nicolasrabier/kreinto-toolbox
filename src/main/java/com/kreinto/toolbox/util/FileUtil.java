package com.kreinto.toolbox.util;

import lombok.extern.slf4j.Slf4j;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class FileUtil {
    
    public static Properties loadProperties(String path) {
        try (InputStream input = new FileInputStream(path)) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            return prop;

        } catch (IOException ex) {
            log.error(ex.getMessage() + "/n" + ex.getStackTrace());
            return new Properties();
        }

    }

}
