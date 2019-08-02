package scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import utils.Constants;

public class DriverTest {
    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", Constants.filesDir + "geckodriver.exe");
        FirefoxProfile ini = new ProfilesIni().getProfile("selenium");
        FirefoxOptions firefoxOptions = new FirefoxOptions().setProfile(ini);
        WebDriver driver = new FirefoxDriver(firefoxOptions);
        try {
            driver.get("");
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
