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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ManageWpScrapper {

    public static final String SERVER_PATH = "app.config.managewp.server.path";
    public static final String LOGIN = "app.config.managewp.login";
    public static final String PASSWORD = "app.config.managewp.password";

    private final static String LOGIN_URL = "/login";
    // private final static String OVERVIEW_URL  = "/dashboard/overview";
    // private final static String OVERVIEW_URL  = "/dashboard/site/2277039/dashboard"; // Donna Ng
    // private final static String OVERVIEW_URL  = "/dashboard/site/2283710/dashboard"; // Petite Chinoise
    private final static String OVERVIEW_URL = "/dashboard/websites?type=thumbnail";

    public static final String DIV_CLASS_SITE_NAME_SPAN = "//div[@class='site-name']//span";
    public static final String DIV_NG_CLICK_UPDATE_ALL_$_EVENT = "//div[@ng-click='updateAll($event)']";
    public static final String BUTTON_CALL_TO_ACTION_TEXT_UPDATE = "//button[@call-to-action-text='Update']";
    public static final String BUTTON_NG_CLICK_SYNC_SITES = "//button[@ng-click='syncSites()']";
    public static final String MWP_SITE_STATUS_ICON_SPAN = "//mwp-site-status-icon//span";
    public static final String I_ANALYTICS_EVENT_OPEN_SINGLE_SITE = "//i[@analytics-event='Open Single Site']";
    public static final String SPAN_CONTAINS_CLASS_USER_NAME = "//span[contains(@class,'user-name')]";
    public static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";

    public ManageWpScrapper() {
        Properties props = FileUtil.loadPropertiesFromResources("my.properties");
        final String serverUrl = props.getProperty(SERVER_PATH);
        try {
            System.setProperty(WEBDRIVER_CHROME_DRIVER, props.getProperty(WEBDRIVER_CHROME_DRIVER));

            final ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("window-size=1200x600");

            final WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
            driver.get(String.format("%s%s", serverUrl, LOGIN_URL));

            if (driver.findElement(By.name("email")) != null &&
                    driver.findElement(By.name("password")) != null &&
                    driver.findElement(By.id("sign-in-button")) != null) {
                driver.findElement(By.name("email")).sendKeys(props.getProperty(LOGIN));
                driver.findElement(By.name("password")).sendKeys(props.getProperty(PASSWORD));
                driver.findElement(By.id("sign-in-button")).click();

                final WebElement usernameElement = driver.findElement(By.xpath(SPAN_CONTAINS_CLASS_USER_NAME));
                if (props.getProperty(LOGIN).equals(usernameElement.getAttribute("uib-tooltip"))) {
                    // once logged in go to list of websites
                    driver.get(String.format("%s%s", serverUrl, OVERVIEW_URL));

                    log.debug("Test if refresh button is spinning.");
                    final WebElement refreshButton = driver.findElement(By.xpath(BUTTON_NG_CLICK_SYNC_SITES));

                    WebDriverWait wait = new WebDriverWait(driver, 600);
                    ExpectedCondition<Boolean> syncingIsOver = arg0 -> !"disabled".equals(refreshButton.getAttribute("disabled"));
                    wait.until(syncingIsOver);


                    List<WebElement> websites = driver.findElements(By.xpath(I_ANALYTICS_EVENT_OPEN_SINGLE_SITE));
                    List<String> websiteDashboardUrls = websites.stream().map(we -> we.getAttribute("href")).collect(Collectors.toList());

                    for (String websiteDashboardUrl : websiteDashboardUrls) {
                        log.info(String.format("go to: %s%s", serverUrl, websiteDashboardUrl));
                        driver.get(String.format("%s%s", serverUrl, websiteDashboardUrl));

                        WebElement siteName = driver.findElement(By.xpath(DIV_CLASS_SITE_NAME_SPAN));
                        log.info(String.format("site name: %s", siteName.getText()));
                        WebElement siteStatus = driver.findElement(By.xpath(MWP_SITE_STATUS_ICON_SPAN));
                        log.info(String.format("site status: %s", siteStatus.getAttribute("uib-tooltip")));
                        WebElement siteNotes = driver.findElement(By.xpath("//textarea[@ng-model='currentNote']"));
                        log.info(String.format("site notes: %s", siteNotes.getText()));

                        if (!siteStatus.getAttribute("class").contains("status-ok")) {
                            try {
                                WebElement updateAllButton = driver.findElement(By.xpath(DIV_NG_CLICK_UPDATE_ALL_$_EVENT));
                                // updateAllButton.click();
                                log.debug("updateAllButton enabled: " + updateAllButton.isEnabled());

                                WebElement confirmUpdateButton = driver.findElement(By.xpath(BUTTON_CALL_TO_ACTION_TEXT_UPDATE));
                                // confirmUpdateButton.click();

                            } catch (NoSuchElementException e) {
                                log.info("Everything is up to date.");
                            }

                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error(ExceptionUtil.format(e));
        }
    }

    public static void main(String[] args) {
        new ManageWpScrapper();
    }
}
