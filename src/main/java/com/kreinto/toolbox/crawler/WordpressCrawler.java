package com.kreinto.toolbox.crawler;

import com.kreinto.toolbox.crawler.managewp.ManageWpCrawler;
import com.kreinto.toolbox.crawler.managewp.ManageWpUtil;
import com.kreinto.toolbox.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Properties;

@Slf4j
public class WordpressCrawler implements Runnable {
    public static final String LI_ID_WP_ADMIN_BAR_W_3_TC_FLUSH_ALL_A = "//li[@id='wp-admin-bar-w3tc_flush_all']//a";
    public static final String LI_ID_WP_ADMIN_BAR_LITESPEED_PURGE_ALL_A = "//li[@id='wp-admin-bar-litespeed-purge-all']//a";

    // WordpressCache: - W3 Total Cache - LiteSpeed Cache

    /*
    TODO:
    - wait til updates are done
    - go to wp admin
    - flush W3 Total Cache if possible then exit
    - flush Lite Speed Cache if possible then exit
     */
    private WebDriver driver;
    private String wpAdminUrl;

    public WordpressCrawler(WebDriver driver, String wpAdminUrl) {
        this.driver = driver;
        this.wpAdminUrl = wpAdminUrl;
        log.info(String.format("wpAdminUrl: %s", wpAdminUrl));
    }

    @Override
    public void run() {
        boolean cacheFlushed = false;

        driver.get(wpAdminUrl);

        // w3tc
        cacheFlushed |= flushCache(LI_ID_WP_ADMIN_BAR_W_3_TC_FLUSH_ALL_A);
        // litespeed cache
        cacheFlushed |= cacheFlushed ? true : flushCache(LI_ID_WP_ADMIN_BAR_LITESPEED_PURGE_ALL_A);

        if(!cacheFlushed) {
            log.warn("Wordpress cache has NOT been flushed.");
        }

    }

    private boolean flushCache(String xpath){
        try {
            final WebElement flushAllLink = driver.findElement(By.xpath(xpath));
            flushAllLink.click();
            return true;
        } catch (NoSuchElementException e) {

        }
        return false;
    }

    public static void main(String[] args) {
        Properties props;
        WebDriver driver;

        props = FileUtil.loadPropertiesFromResources("my.properties");
        driver = CrawlerBuilder.buildWebDriver(props);

        driver.get(args[0]);

        ManageWpUtil.signingIn(driver, props);

        WordpressCrawler scrapper = new WordpressCrawler(null, args[0]);
        scrapper.run();
    }

}
