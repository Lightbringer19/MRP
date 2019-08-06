package scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import utils.Constants;

import java.util.concurrent.TimeUnit;

public class DriverTest {
    
    private static WebDriver driver;
    
    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
        // FirefoxProfile ini = new ProfilesIni().getProfile("selenium");
        // FirefoxOptions firefoxOptions = new FirefoxOptions().setProfile(ini);
        // driver = new FirefoxDriver(firefoxOptions);
        driver = new FirefoxDriver();
        // String videosUrl = "https://www.bpmsupreme.com/store/newreleases/video/classic/1";
        try {
            driver.get("https://www.bpmsupreme.com/login");
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            Thread.sleep(10000);
            By submitButtonNavigator = By.tagName("button");
            WebElement webElement = driver.findElement(submitButtonNavigator);
            webElement.click();
            
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
