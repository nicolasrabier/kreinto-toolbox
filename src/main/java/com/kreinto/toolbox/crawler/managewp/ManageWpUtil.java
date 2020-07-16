package com.kreinto.toolbox.crawler.managewp;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class ManageWpUtil {

    public static final String BUTTON_NG_CLICK_SYNC_SITES = "//button[@ng-click='syncSites()']";

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

}
