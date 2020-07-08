package com.kreinto.toolbox.scrapper;

import com.kreinto.toolbox.util.EmailSender;
import com.kreinto.toolbox.util.ExceptionUtil;
import com.kreinto.toolbox.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class ManageWpScrapper {

    public static final String SERVER_PATH = "app.config.managewp.server.path";
    public static final String LOGIN = "app.config.managewp.login";
    public static final String PASSWORD = "app.config.managewp.password";

    //TODO: set these variables in the config file
    private final static String LOGIN_URL = "/login";
    // private final static String OVERVIEW_URL  = "/dashboard/overview";
    // private final static String OVERVIEW_URL  = "/dashboard/site/2277039/dashboard"; // Donna Ng
    private final static String OVERVIEW_URL  = "/dashboard/site/2283710/dashboard"; // Petite Chinoise

    public ManageWpScrapper() {
        Properties props = FileUtil.loadProperties("my.properties");

        try{
            System.setProperty("webdriver.chrome.driver", "/Users/nic/kreinto/projects/toolbox/drivers/macos/chromedriver");

            final ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("window-size=1200x600");

            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
            driver.get(new StringBuilder(props.getProperty(SERVER_PATH)).append(LOGIN_URL).toString());

            if(driver.findElement(By.name("email")) != null &&
                    driver.findElement(By.name("password")) != null &&
                    driver.findElement(By.id("sign-in-button")) != null) {
                driver.findElement(By.name("email")).sendKeys(props.getProperty(LOGIN));
                driver.findElement(By.name("password")).sendKeys(props.getProperty(PASSWORD));
                driver.findElement(By.id("sign-in-button")).click();

                // test if properly logged in
                WebElement bodyElement = driver.findElement(By.id("managewp-orion"));
                if (bodyElement != null && bodyElement.isDisplayed()) {
                    driver.get(new StringBuilder(props.getProperty(SERVER_PATH)).append(OVERVIEW_URL).toString());

                    WebElement refreshButton = driver.findElement(By.xpath("//button[@ng-click='syncSites()']"));
                    if ("disabled".equals(refreshButton.getAttribute("disabled"))) {
                        Wait wait = new FluentWait(driver).withTimeout(Duration.ofSeconds(280)).pollingEvery(Duration.ofSeconds(5)).ignoring(NoSuchElementException.class);
                        refreshButton = (WebElement) wait.until(new Function<WebDriver, WebElement>() {
                            public WebElement apply(WebDriver driver) {
                                WebElement refreshButton = driver.findElement(By.xpath("//button[@ng-click='syncSites()']"));
                                if (refreshButton != null && "disabled".equals(refreshButton.getAttribute("disabled"))) {
                                    return null;
                                } else {
                                    return refreshButton;
                                }
                            }
                        });

                        // WebDriverWait wait2 = new WebDriverWait(driver, 280);
                        // wait2.until(ExpectedConditions.elementToBeClickable(refreshButton));
                    }

                    WebElement updateAllButton = driver.findElement(By.xpath("//div[@ng-click='updateAll($event)']"));
                    updateAllButton.click();
                    System.out.println("updateAllButton enabled: " + updateAllButton.isEnabled());

                    WebElement confirmUpdateButton = driver.findElement(By.xpath("//button[@call-to-action-text='Update']"));
                    confirmUpdateButton.click();



                } else {
                    new Exception("The webdriver cannot find the main div named 'managewp-orion'.");
                }
            }
        } catch (Exception e){
            log.error(ExceptionUtil.format(e));
        }
    }

    public static void main(String[] args) {
        new ManageWpScrapper();
    }
}
