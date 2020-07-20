package com.kreinto.toolbox.crawler.managewp;

import com.kreinto.toolbox.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Properties;
import java.util.Set;

@Slf4j
public class ManageWpUtil {

    public static final String LOGIN = "app.config.managewp.login";
    public static final String PASSWORD = "app.config.managewp.password";

    public static final String BUTTON_NG_CLICK_SYNC_SITES = "//button[@ng-click='syncSites()']";
    private static final String SPAN_CONTAINS_CLASS_USER_NAME = "//span[contains(@class,'user-name')]";

    protected static void waitUntilSyncingIsOver(WebDriver driver) {
        log.debug("Test if refresh button is spinning.");
        final WebElement refreshButton = driver.findElement(By.xpath(BUTTON_NG_CLICK_SYNC_SITES));
        WebDriverWait wait = new WebDriverWait(driver, 600);
        ExpectedCondition<Boolean> syncingIsOver = arg0 -> refreshButton.getAttribute("disabled") == null && getInitialSplashScreen(driver) == null;
        wait.until(syncingIsOver);
    }

    private static WebElement getInitialSplashScreen(WebDriver driver) {
        try {
            return driver.findElement(By.id("initial-splash-screen"));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static boolean signingIn(WebDriver driver, Properties props){
        driver.findElement(By.name("email")).sendKeys(props.getProperty(LOGIN));
        driver.findElement(By.name("password")).sendKeys(props.getProperty(PASSWORD));
        driver.findElement(By.id("sign-in-button")).click();

        final WebElement usernameElement = driver.findElement(By.xpath(SPAN_CONTAINS_CLASS_USER_NAME));
        if (props.getProperty(LOGIN).equals(usernameElement.getAttribute("uib-tooltip"))) {
            return true;
        }
        return false;
    }

}
