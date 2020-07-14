package com.kreinto.toolbox.crawler;

import com.kreinto.toolbox.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Properties;

@Slf4j
public class WordpressCrawler implements Runnable {

    // WordpressCache: - W3 Total Cache - LiteSpeed Cache

    /*
    TODO:
    - wait til updates are done
    - go to wp admin
    - flush W3 Total Cache if possible then exit
    - flush Lite Speed Cache if possible then exit
     */
    private WebDriver driver;
    private String wpAdminUrl;

    public WordpressCrawler(WebDriver driver, String wpAdminUrl) {
        this.driver = driver;
        this.wpAdminUrl = wpAdminUrl;
        log.info(String.format("wpAdminUrl: %s", wpAdminUrl));
    }

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        Properties props;
        WebDriver driver;

        props = FileUtil.loadPropertiesFromResources("my.properties");
        driver = CrawlerBuilder.buildWebDriver(props);

        driver.get(args[0]);

        driver.findElement(By.name("email")).sendKeys(props.getProperty(ManageWpCrawler.LOGIN));
        driver.findElement(By.name("password")).sendKeys(props.getProperty(ManageWpCrawler.PASSWORD));
        driver.findElement(By.id("sign-in-button")).click();

        WordpressCrawler scrapper = new WordpressCrawler(null, args[0]);
        scrapper.run();
    }

}
