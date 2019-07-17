package scraper.mp3pool;

import configuration.YamlConfig;
import mongodb.MongoControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.Constants;
import utils.Logger;

import java.util.concurrent.TimeUnit;

public class Mp3poolDriver {

    private static String USERNAME;
    private static String PASS;
    private WebDriver driver;
    static MongoControl mongoControl = new MongoControl();
    static Logger mp3Logger = new Logger("8thWonder");

    public Mp3poolDriver() {
        String pathToSelenium = Constants.filesDir + "geckodriver.exe";
        System.setProperty("webdriver.gecko.driver", pathToSelenium);
        YamlConfig yamlConfig = new YamlConfig();

        USERNAME = yamlConfig.config.getMp3_pool_username();
        PASS = yamlConfig.config.getMp3_pool_password();
    }

    private void Login() {
        mp3Logger.log("Login");
        driver.get("https://mp3poolonline.com/user/login");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        // Enter Username
        WebElement nameField = driver.findElement(By.id("edit-name"));
        nameField.sendKeys(USERNAME);
        // Enter Password
        WebElement passwordField = driver.findElement(By.id("edit-pass"));
        passwordField.sendKeys(PASS);
        // Click Login
        driver.findElement(By.id("edit-submit")).click();
    }
}
