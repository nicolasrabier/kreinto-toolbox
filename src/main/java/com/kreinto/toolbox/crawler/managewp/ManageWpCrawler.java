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

    private final static String LOGIN_URL = "/login";
    private final static String OVERVIEW_URL = "/dashboard/websites?type=thumbnail";

    private static final String I_ANALYTICS_EVENT_OPEN_SINGLE_SITE = "//i[@analytics-event='Open Single Site']";

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

    public void updateAllWebsites() {
        try {
            // login
            driver.get(String.format("%s%s", serverUrl, LOGIN_URL));

            if(ManageWpUtil.signingIn(driver, props)) {
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
                        executorService.submit(new ManageWpWebsiteCrawler(props, driver.manage().getCookies(), mwpWebsiteDashboardUrl));
                        Thread.currentThread().sleep(5000);
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
