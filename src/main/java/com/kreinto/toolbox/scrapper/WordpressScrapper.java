package com.kreinto.toolbox.scrapper;

import com.kreinto.toolbox.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WordpressScrapper implements Runnable {

    // WordpressCache: - W3 Total Cache - LiteSpeed Cache

    /*
    TODO:
    - wait til updates are done
    - go to wp admin
    - flush W3 Total Cache if possible then exit
    - flush Lite Speed Cache if possible then exit
     */
    private WebDriver driver;
    private String mwpDashboardUrl;
    private String wpAdminUrl;

    public WordpressScrapper(WebDriver driver, String mwpDashboardUrl, String wpAdminUrl) {
        this.driver = driver;
        this.mwpDashboardUrl = mwpDashboardUrl;
        this.wpAdminUrl = wpAdminUrl;
    }

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        Properties props;
        String serverUrl;
        ChromeOptions options;
        WebDriver driver;

        // login into manage wp
        props = FileUtil.loadPropertiesFromResources("my.properties");
        serverUrl = props.getProperty(ManageWpScrapper.SERVER_PATH);

        System.setProperty(ManageWpScrapper.WEBDRIVER_CHROME_DRIVER, props.getProperty(ManageWpScrapper.WEBDRIVER_CHROME_DRIVER));

        options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1200x600");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        driver.get(args[0]);

        driver.findElement(By.name("email")).sendKeys(props.getProperty(ManageWpScrapper.LOGIN));
        driver.findElement(By.name("password")).sendKeys(props.getProperty(ManageWpScrapper.PASSWORD));
        driver.findElement(By.id("sign-in-button")).click();

        WordpressScrapper scrapper = new WordpressScrapper(null, args[0], args[1]);
        scrapper.run();
    }

}
