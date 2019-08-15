package scraper;

import configuration.YamlConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import utils.Constants;

import java.util.concurrent.TimeUnit;

public class DriverTest {
    
    private static WebDriver driver;
    
    protected static YamlConfig.Config yamlConfig = new YamlConfig().config;
    
    public static void main(String[] args) {
        String USERNAME = yamlConfig.getDalemasbajo_username();
        String PASS = yamlConfig.getDalemasbajo_password();
        System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
        // FirefoxProfile ini = new ProfilesIni().getProfile("selenium");
        // FirefoxOptions firefoxOptions = new FirefoxOptions().setProfile(ini);
        // driver = new FirefoxDriver(firefoxOptions);
        driver = new FirefoxDriver();
        try {
            driver.get("https://dalemasbajo.com/");
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            // Cookie cookie = LoginInterface.getCookie(USERNAME, PASS);
            driver.manage().deleteAllCookies();
            // driver.manage().addCookie(cookie);
            driver.get("https://dalemasbajo.com/");
            // By submitButtonNavigator = By.partialLinkText("Ingresar");
            // WebElement webElement = driver.findElement(submitButtonNavigator);
            // webElement.click();
            
            Thread.sleep(10000);
            // String pageSource = getPageSource();
            // System.out.println(pageSource);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
    
    private static String getPageSource() {
        String javascript = "return document.getElementsByTagName('html')[0].innerHTML";
        String pageSource = (String) ((JavascriptExecutor) driver).executeScript(javascript,
           driver.findElement(By.tagName("html")));
        return "<html>" + pageSource + "</html>";
    }
}
