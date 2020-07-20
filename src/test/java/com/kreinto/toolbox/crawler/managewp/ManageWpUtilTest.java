package com.kreinto.toolbox.crawler.managewp;

import com.kreinto.toolbox.crawler.CrawlerBuilder;
import com.kreinto.toolbox.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ManageWpUtilTest {

    @Test
    void waitUntilSyncingIsOver() {
    }

    @Test
    void signingInSimple() {
        Properties props = FileUtil.loadPropertiesFromResources("my.properties");
        String serverUrl = props.getProperty(ManageWpCrawler.SERVER_PATH);
        WebDriver driver = CrawlerBuilder.buildWebDriver(props);
        driver.get(String.format("%s%s", serverUrl, ManageWpCrawler.LOGIN_URL));
        assertTrue(ManageWpUtil.signingIn(driver, props));
    }
}