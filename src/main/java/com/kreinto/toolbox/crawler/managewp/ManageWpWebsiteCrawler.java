package com.kreinto.toolbox.crawler.managewp;

import com.kreinto.toolbox.crawler.CrawlerBuilder;
import com.kreinto.toolbox.crawler.WordpressCrawler;
import com.kreinto.toolbox.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class ManageWpWebsiteCrawler implements Runnable {

    private static final String DIV_CLASS_SITE_NAME_SPAN = "//div[@class='site-name']//span";
    private static final String DIV_NG_CLICK_UPDATE_ALL_$_EVENT = "//div[@ng-click='updateAll($event)']";
    private static final String BUTTON_CALL_TO_ACTION_TEXT_UPDATE = "//button[@call-to-action-text='Update']";
    private static final String MWP_SITE_STATUS_ICON_SPAN = "//mwp-site-status-icon//span";
    private static final String TEXTAREA_NG_MODEL_CURRENT_NOTE = "//textarea[@ng-model='currentNote']";
    private static final String A_ADMIN_SITE_SITE = "//a[@admin-site='site']";

    private Properties props;
    private WebDriver driver;
    private WebDriverWait wait;
    private String mwpWebsiteDashboardUrl;

    public ManageWpWebsiteCrawler(Properties props, Set<Cookie> cookies, String mwpWebsiteDashboardUrl) {
        this.props = props;
        driver = CrawlerBuilder.buildWebDriver(props);
        this.mwpWebsiteDashboardUrl = mwpWebsiteDashboardUrl;
    }

    @Override
    public void run() {
        logMsg(String.format("> Start crawler on: %s", mwpWebsiteDashboardUrl));
        driver.get(mwpWebsiteDashboardUrl);

        if(ManageWpUtil.signingIn(driver, props)) {
            driver.get(mwpWebsiteDashboardUrl);

            WebElement siteName = driver.findElement(By.xpath(DIV_CLASS_SITE_NAME_SPAN));
            logMsg(String.format("site name: %s", siteName.getText()));

            final WebElement siteStatus = driver.findElement(By.xpath(MWP_SITE_STATUS_ICON_SPAN));
            String tmpStatus = siteStatus.getAttribute("uib-tooltip");
            logMsg(String.format("site status before: %s", tmpStatus));
            wait = new WebDriverWait(driver, 180);
            ExpectedCondition<Boolean> statusHasChanged = arg0 -> !tmpStatus.equals(siteStatus.getAttribute("uib-tooltip"));
            try {
                wait.until(statusHasChanged);
            } catch (TimeoutException e) {

            }

            logMsg(String.format("site status: %s", siteStatus.getAttribute("uib-tooltip")));
            logMsg(String.format("site status class: %s", siteStatus.getAttribute("class")));

            WebElement siteNotes = driver.findElement(By.xpath(TEXTAREA_NG_MODEL_CURRENT_NOTE));
            logMsg(String.format("site notes: %s", siteNotes.getAttribute("value")));
            List<String> tags = Arrays.asList(siteNotes.getAttribute("value").split("\n"));

            if (siteNotes.getAttribute("value").contains(ManageWpTag.NO_UPDATE.toString())) {
                logMsg(String.format("no update on the website"));
            } else {
                if (!siteStatus.getAttribute("class").contains("status-ok")) {
                    ManageWpUtil.waitUntilSyncingIsOver(driver);
                    WebElement updateAllButton = driver.findElement(By.xpath(DIV_NG_CLICK_UPDATE_ALL_$_EVENT));
                    logMsg(String.format("perform update all: %s", updateAllButton.getAttribute("uib-tooltip")));
                    updateAllButton.click();

                    WebElement confirmUpdateButton = driver.findElement(By.xpath(BUTTON_CALL_TO_ACTION_TEXT_UPDATE));
                    logMsg("confirm update all");
                    confirmUpdateButton.click();

                    if(!tags.contains("no-cache")) {
                        //executorService.submit(new ManageWpWaitForUpdate(driver.manage().getCookies(), mwpWebsiteDashboardUrl));
                        driver.get(mwpWebsiteDashboardUrl);

                        // wait
                        final WebElement stateTitle = driver.findElement(By.xpath("//h4[@class='empty-state-title']"));
                        wait = new WebDriverWait(driver, 600);
                        ExpectedCondition<Boolean> updatingIsOver = arg0 -> !(stateTitle.getText().equals("Updating Plugins"));
                        wait.until(updatingIsOver);

                        WebElement wpAdminButton = driver.findElement(By.xpath(A_ADMIN_SITE_SITE));

                        WordpressCrawler wpCrawler = new WordpressCrawler(driver, wpAdminButton.getAttribute("href"));
                        wpCrawler.run();
                    }
                }
            }
        }
        logMsg(String.format("< End crawler on: %s", mwpWebsiteDashboardUrl));
    }

    private void logMsg(String message){
        log.info(String.format("%d | %s", Thread.currentThread().getId(), message));
    }
}
