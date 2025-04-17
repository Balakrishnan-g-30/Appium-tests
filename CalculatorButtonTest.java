package appiumtest;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.net.MalformedURLException;

public class CalculatorButtonTest {
    public static void main(String[] args) throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        
        // Device and app configuration
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:deviceName", "OnePlus 6T");
        caps.setCapability("appium:platformVersion", "11.1.2.2");
        caps.setCapability("appium:udid", "72b84a4d");
        caps.setCapability("appium:automationName", "UiAutomator2");
        caps.setCapability("appium:appPackage", "com.oneplus.calculator");
        caps.setCapability("appium:appActivity", "com.oneplus.calculator.Calculator");

        // Appium server
        URL appiumServerURL = new URL("http://127.0.0.1:4723/wd/hub");
        AndroidDriver<MobileElement> driver = new AndroidDriver<>(appiumServerURL, caps);

        try {
            System.out.println("Calculator app is launched. Performing calculation 2 + 3 =");

            // Click '2'
            driver.findElementById("com.oneplus.calculator:id/digit_2").click();

            // Click '+'
            driver.findElementByAccessibilityId("plus").click();

            // Click '3'
            driver.findElementById("com.oneplus.calculator:id/digit_3").click();

            // Click '='
            driver.findElementByAccessibilityId("equals").click();

            // Get the result text
            String result = driver.findElementById("com.oneplus.calculator:id/result").getText();
            System.out.println("Calculation Result: " + result);

            Thread.sleep(5000); // Wait to visually verify result

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
