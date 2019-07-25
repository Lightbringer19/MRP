package scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import utils.Constants;

import java.util.concurrent.TimeUnit;

public class DriverTest {
    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
        
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get("https://beatjunkies.com/login");
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            Thread.sleep(2000);
            driver.findElement(By.xpath("//*[@id=\"recaptcha-anchor\"]")).click();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
