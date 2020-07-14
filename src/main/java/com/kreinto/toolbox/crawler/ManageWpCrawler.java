package com.kreinto.toolbox.crawler;

import com.kreinto.toolbox.util.ExceptionUtil;
import com.kreinto.toolbox.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class ManageWpCrawler {

    public static final String SERVER_PATH = "app.config.managewp.server.path";
    public static final String LOGIN = "app.config.managewp.login";
    public static final String PASSWORD = "app.config.managewp.password";

    private final static String LOGIN_URL = "/login";
    private final static String OVERVIEW_URL = "/dashboard/websites?type=thumbnail";

    private static final String DIV_CLASS_SITE_NAME_SPAN = "//div[@class='site-name']//span";
    private static final String DIV_NG_CLICK_UPDATE_ALL_$_EVENT = "//div[@ng-click='updateAll($event)']";
    private static final String BUTTON_CALL_TO_ACTION_TEXT_UPDATE = "//button[@call-to-action-text='Update']";
    private static final String BUTTON_NG_CLICK_SYNC_SITES = "//button[@ng-click='syncSites()']";
    private static final String MWP_SITE_STATUS_ICON_SPAN = "//mwp-site-status-icon//span";
    private static final String I_ANALYTICS_EVENT_OPEN_SINGLE_SITE = "//i[@analytics-event='Open Single Site']";
    private static final String SPAN_CONTAINS_CLASS_USER_NAME = "//span[contains(@class,'user-name')]";
    private static final String TEXTAREA_NG_MODEL_CURRENT_NOTE = "//textarea[@ng-model='currentNote']";
    private static final String A_ADMIN_SITE_SITE = "//a[@admin-site='site']";

    private Properties props;
    private String serverUrl;
    private WebDriver driver;
    private WebDriverWait wait;
    final private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public ManageWpCrawler() {
        props = FileUtil.loadPropertiesFromResources("my.properties");
        serverUrl = props.getProperty(SERVER_PATH);

        driver = CrawlerBuilder.buildWebDriver(props);

    }

    public boolean signingIn(WebDriver driver){
        try {
            // login
            driver.get(String.format("%s%s", serverUrl, LOGIN_URL));

            driver.findElement(By.name("email")).sendKeys(props.getProperty(LOGIN));
            driver.findElement(By.name("password")).sendKeys(props.getProperty(PASSWORD));
            driver.findElement(By.id("sign-in-button")).click();

            final WebElement usernameElement = driver.findElement(By.xpath(SPAN_CONTAINS_CLASS_USER_NAME));
            if (props.getProperty(LOGIN).equals(usernameElement.getAttribute("uib-tooltip"))) {
                return true;
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.format(e));
        } finally {
            return false;
        }
    }

    public void updateAllWebsites() {
        try {
            if(signingIn(driver)) {
                // once logged in go to list of websites
                driver.get(String.format("%s%s", serverUrl, OVERVIEW_URL));

                waitUntilSyncingIsOver(driver);
                driver.findElement(By.xpath(BUTTON_NG_CLICK_SYNC_SITES)).click();
                waitUntilSyncingIsOver(driver);

                List<WebElement> websites = driver.findElements(By.xpath(I_ANALYTICS_EVENT_OPEN_SINGLE_SITE));
                List<String> websiteDashboardUrls = websites.stream().map(we -> we.getAttribute("href")).collect(Collectors.toList());

                for (String websiteDashboardUrl : websiteDashboardUrls) {
                    try {
                        String mwpWebsiteDashboardUrl = String.format("%s%s", serverUrl, websiteDashboardUrl);
                        log.info(String.format("go to: %s", mwpWebsiteDashboardUrl));
                        driver.get(mwpWebsiteDashboardUrl);

                        WebElement siteName = driver.findElement(By.xpath(DIV_CLASS_SITE_NAME_SPAN));
                        log.info(String.format("site name: %s", siteName.getText()));

                        final WebElement siteStatus = driver.findElement(By.xpath(MWP_SITE_STATUS_ICON_SPAN));
                        String tmpStatus = siteStatus.getAttribute("uib-tooltip");
                        wait = new WebDriverWait(driver, 60);
                        ExpectedCondition<Boolean> statusHasChanged = arg0 -> !tmpStatus.equals(siteStatus.getAttribute("uib-tooltip"));
                        wait.until(statusHasChanged);

                        log.info(String.format("site status: %s", siteStatus.getAttribute("uib-tooltip")));
                        log.info(String.format("site status class: %s", siteStatus.getAttribute("class")));

                        WebElement siteNotes = driver.findElement(By.xpath(TEXTAREA_NG_MODEL_CURRENT_NOTE));
                        log.info(String.format("site notes: %s", siteNotes.getAttribute("value")));

                        if (siteNotes.getAttribute("value").contains(ManageWpTag.NO_UPDATE.toString())) {
                            log.info(String.format("no update on the website"));
                        } else {
                            if (!siteStatus.getAttribute("class").contains("status-ok")) {
                                waitUntilSyncingIsOver(driver);
                                WebElement updateAllButton = driver.findElement(By.xpath(DIV_NG_CLICK_UPDATE_ALL_$_EVENT));
                                log.info(String.format("perform update all: %s", updateAllButton.getAttribute("uib-tooltip")));
                                updateAllButton.click();

                                WebElement confirmUpdateButton = driver.findElement(By.xpath(BUTTON_CALL_TO_ACTION_TEXT_UPDATE));
                                log.info("confirm update all");
                                confirmUpdateButton.click();

                                executorService.submit(new ManageWpWaitForUpdate(driver.manage().getCookies(), mwpWebsiteDashboardUrl));
                            }
                        }
                    } catch (NoSuchElementException e) {
                        log.error(ExceptionUtil.format(e));
                    }
                }
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.format(e));
        }
    }

    private void waitUntilSyncingIsOver(WebDriver driver) {
        log.debug("Test if refresh button is spinning.");
        final WebElement refreshButton = driver.findElement(By.xpath(BUTTON_NG_CLICK_SYNC_SITES));
        WebDriverWait wait = new WebDriverWait(driver, 600);
        ExpectedCondition<Boolean> syncingIsOver = arg0 -> refreshButton.getAttribute("disabled") == null && getInitialSplashScreen(driver) == null;
        wait.until(syncingIsOver);
    }

    private WebElement getInitialSplashScreen(WebDriver driver) {
        try {
            return driver.findElement(By.id("initial-splash-screen"));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        ManageWpCrawler scrapper = new ManageWpCrawler();
        scrapper.updateAllWebsites();
    }

}