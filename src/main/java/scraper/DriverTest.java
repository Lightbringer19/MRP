package scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import utils.Constants;

public class DriverTest {
    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
        
        WebDriver driver = new FirefoxDriver();
        try {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
