package appiumtest;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;
import java.net.MalformedURLException;

public class AppiumTest {
    public static void main(String[] args) throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();

        // W3C capabilities
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:deviceName", "OnePlus 6T");
        caps.setCapability("appium:platformVersion", "11.1.2.2");
        caps.setCapability("appium:udid", "72b84a4d");
        caps.setCapability("appium:automationName", "UiAutomator2");
        caps.setCapability("appium:appPackage", "com.oneplus.calculator");
        caps.setCapability("appium:appActivity", "com.oneplus.calculator.Calculator");

        // Appium server URL
        URL appiumServerURL = new URL("http://127.0.0.1:4723/wd/hub");
        AndroidDriver<MobileElement> driver = new AndroidDriver<>(appiumServerURL, caps);

        
        System.out.println("Calculator app is launched. Waiting for 30 seconds...");

        // Keep app open for 30 seconds
        try {
            Thread.sleep(30000); // 30,000 milliseconds = 30 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.quit();
    }
}
