package com.kreinto.toolbox.scrapper;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

@Slf4j
public class WordpressScrapper implements Runnable {

    // WordpressCache: - W3 Total Cache - LiteSpeed Cache

    /*
    TODO:
    - wait til updates are done
    - go to wp admin
    - flush W3 Total Cache if possible then exit
    - flush Lite Speed Cache if possible then exit
     */
    private WebDriver driver;
    private String mwpDashboardUrl;
    private String wpAdminUrl;

    public WordpressScrapper(WebDriver driver, String mwpDashboardUrl, String wpAdminUrl) {
        this.driver = driver;
        this.mwpDashboardUrl = mwpDashboardUrl;
        this.wpAdminUrl = wpAdminUrl;
    }

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        // login into manage wp


        WordpressScrapper scrapper = new WordpressScrapper(null,"", "");
        scrapper.run();
    }


}
