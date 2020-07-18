package com.kreinto.toolbox.crawler.managewp;

import com.kreinto.toolbox.crawler.CrawlerBuilder;
import com.kreinto.toolbox.crawler.WordpressCrawler;
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

    private static final String I_ANALYTICS_EVENT_OPEN_SINGLE_SITE = "//i[@analytics-event='Open Single Site']";
    private static final String SPAN_CONTAINS_CLASS_USER_NAME = "//span[contains(@class,'user-name')]";

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
        }
        return false;
    }

    public void updateAllWebsites() {
        try {
            if(signingIn(driver)) {
                // once logged in go to list of websites
                driver.get(String.format("%s%s", serverUrl, OVERVIEW_URL));

                ManageWpUtil.waitUntilSyncingIsOver(driver);
                driver.findElement(By.xpath(ManageWpUtil.BUTTON_NG_CLICK_SYNC_SITES)).click();
                ManageWpUtil.waitUntilSyncingIsOver(driver);

                List<WebElement> websites = driver.findElements(By.xpath(I_ANALYTICS_EVENT_OPEN_SINGLE_SITE));
                List<String> websiteDashboardUrls = websites.stream().map(we -> we.getAttribute("href")).collect(Collectors.toList());

                for (String websiteDashboardUrl : websiteDashboardUrls) {
                    try {
                        String mwpWebsiteDashboardUrl = String.format("%s%s", serverUrl, websiteDashboardUrl);
                        log.info(String.format("go to: %s", mwpWebsiteDashboardUrl));

                        executorService.submit(new ManageWpWebsiteCrawler(props, driver.manage().getCookies(), mwpWebsiteDashboardUrl));

                    } catch (NoSuchElementException e) {
                        log.error(ExceptionUtil.format(e));
                    }
                }
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.format(e));
        }
    }

    public static void main(String[] args) {
        ManageWpCrawler scrapper = new ManageWpCrawler();
        scrapper.updateAllWebsites();
    }

}
