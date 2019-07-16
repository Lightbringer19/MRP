package scraper.dmp;

import configuration.YamlConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.Constants;
import utils.Log;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DmpDriver extends Thread {
    private static String username;
    private static String pass;
    public WebDriver driver;
    static String cookieForAPI;

    public static void main(String[] args) {
    }

    @Override
    public void run() {
        everything();
    }

    private void Login() {
        Log.write("Login", "Scraper");
        driver.get("https://www.digitalmusicpool.com/new_releases");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        // Enter Username
        WebElement nameField = driver.findElement(By.id("EmailAddress"));
        nameField.sendKeys(username);
        // Enter Password
        WebElement passwordField = driver.findElement(By.id("Password"));
        passwordField.sendKeys(pass);
        // Click Login
        driver.findElement(By.id("LoginSubmitButton")).click();
    }

    void everything() {
        YamlConfig yamlConfig = new YamlConfig();
        username = yamlConfig.config.getDmp_username();
        pass = yamlConfig.config.getDmp_password();
        try {
            Log.write("Start", "Scraper");
            //set DB control
            String pathToSelenium = Constants.filesDir + "geckodriver.exe";
            System.setProperty("webdriver.gecko.driver", pathToSelenium);
            driver = new FirefoxDriver(new FirefoxOptions().setHeadless(true));
            Login();
            String url = "https://www.digitalmusicpool.com/new_releases";
            driver.get(url);
            Set<Cookie> cookies = driver.manage().getCookies();
            List<String> cookiesList = cookies.stream().map(cookie ->
                    cookie.getName() + "=" + cookie.getValue()).collect(Collectors.toList());
            cookieForAPI = String.join("; ", cookiesList);
            Log.write(cookieForAPI, "Scraper");
        } catch (Exception e) {
            driver.quit();
        }
    }

    void quitDriver() {
        driver.quit();
    }

}
