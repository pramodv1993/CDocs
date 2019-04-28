package com.cdocs.cdocsocrservice;

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class CDocsProperties {
    private Properties properties = new Properties();
    public CDocsProperties() {
        try (InputStream input = new FileInputStream("src\\main\\resources\\application.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProp(String prop) {
        return properties.getProperty(prop);
    }
}
