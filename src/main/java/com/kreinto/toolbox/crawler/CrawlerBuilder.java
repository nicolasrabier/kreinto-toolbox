package com.kreinto.toolbox.crawler;

import com.kreinto.toolbox.util.FileUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class CrawlerBuilder {

    public static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";

    public static WebDriver buildWebDriver(Properties props) {

        System.setProperty(WEBDRIVER_CHROME_DRIVER, props.getProperty(WEBDRIVER_CHROME_DRIVER));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1200x600");

        final WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        return driver;
    }
}
