package com.kreinto.toolbox.scrapper;

import com.kreinto.toolbox.util.ExceptionUtil;
import com.kreinto.toolbox.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ManageWpScrapper {

    public static final String SERVER_PATH = "app.config.managewp.server.path";
    public static final String LOGIN = "app.config.managewp.login";
    public static final String PASSWORD = "app.config.managewp.password";

    private final static String LOGIN_URL = "/login";
    private final static String OVERVIEW_URL = "/dashboard/websites?type=thumbnail";

    public static final String DIV_CLASS_SITE_NAME_SPAN = "//div[@class='site-name']//span";
    public static final String DIV_NG_CLICK_UPDATE_ALL_$_EVENT = "//div[@ng-click='updateAll($event)']";
    public static final String BUTTON_CALL_TO_ACTION_TEXT_UPDATE = "//button[@call-to-action-text='Update']";
    public static final String BUTTON_NG_CLICK_SYNC_SITES = "//button[@ng-click='syncSites()']";
    public static final String MWP_SITE_STATUS_ICON_SPAN = "//mwp-site-status-icon//span";
    public static final String I_ANALYTICS_EVENT_OPEN_SINGLE_SITE = "//i[@analytics-event='Open Single Site']";
    public static final String SPAN_CONTAINS_CLASS_USER_NAME = "//span[contains(@class,'user-name')]";
    public static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
    public static final String TEXTAREA_NG_MODEL_CURRENT_NOTE = "//textarea[@ng-model='currentNote']";
    public static final String A_ADMIN_SITE_SITE = "//a[@admin-site='site']";

    private Properties props;
    private String serverUrl;
    private ChromeOptions options;
    private WebDriver driver;
    private WebDriverWait wait;
    final private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public ManageWpScrapper() {
        props = FileUtil.loadPropertiesFromResources("my.properties");
        serverUrl = props.getProperty(SERVER_PATH);

        System.setProperty(WEBDRIVER_CHROME_DRIVER, props.getProperty(WEBDRIVER_CHROME_DRIVER));

        options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1200x600");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
    }

    public void updateAllWebsites() {
        try {
            // login
            driver.get(String.format("%s%s", serverUrl, LOGIN_URL));

            driver.findElement(By.name("email")).sendKeys(props.getProperty(LOGIN));
            driver.findElement(By.name("password")).sendKeys(props.getProperty(PASSWORD));
            driver.findElement(By.id("sign-in-button")).click();

            final WebElement usernameElement = driver.findElement(By.xpath(SPAN_CONTAINS_CLASS_USER_NAME));
            if (props.getProperty(LOGIN).equals(usernameElement.getAttribute("uib-tooltip"))) {
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

                        WebElement wpAdminButton = driver.findElement(By.xpath(A_ADMIN_SITE_SITE));

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

                                executorService.submit(new WordpressScrapper(driver, mwpWebsiteDashboardUrl, wpAdminButton.getAttribute("href")));
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
        ManageWpScrapper scrapper = new ManageWpScrapper();
        scrapper.updateAllWebsites();
    }
}
