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
            Properties props = new Properties();
            // load a properties file
            props.load(input);
            return props;
        } catch (IOException ex) {
            log.error(ExceptionUtil.format(ex));
            return new Properties();
        }
    }

    public static Properties loadPropertiesFromResources(String path) {
        try (InputStream input = FileUtil.class.getClassLoader().getResourceAsStream(path)) {
            Properties props = new Properties();
            // load a properties file
            props.load(input);
            return props;
        } catch (IOException ex) {
            log.error(ExceptionUtil.format(ex));
            return new Properties();
        }
    }

}
