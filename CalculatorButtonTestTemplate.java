package appiumtest;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class CalculatorButtonTestTemplate {

    public static void main(String[] args) {
        try {
            String[] appInfo = getCalculatorAppDetails(); // [0] = package, [1] = activity

            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("platformName", "Android");
            caps.setCapability("automationName", "UiAutomator2");
            caps.setCapability("deviceName", "AndroidDevice"); // Required, but can be a placeholder
            caps.setCapability("appPackage", appInfo[0]);
            caps.setCapability("appActivity", appInfo[1]);

            URL appiumServerURL = new URL("http://127.0.0.1:4723/wd/hub");
            AndroidDriver<MobileElement> driver = new AndroidDriver<>(appiumServerURL, caps);
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

            System.out.println("Calculator app is launched. Performing calculation 2 + 3 =");

            driver.findElementByAndroidUIAutomator("new UiSelector().text(\"2\")").click();
            driver.findElementByAndroidUIAutomator("new UiSelector().descriptionContains(\"plus\")").click();
            driver.findElementByAndroidUIAutomator("new UiSelector().text(\"3\")").click();
            driver.findElementByAndroidUIAutomator("new UiSelector().descriptionContains(\"equals\")").click();

            // Fallback: try to find result using partial ID
            MobileElement result = driver.findElementByXPath("//*[contains(@resource-id,'result') or contains(@resource-id,'output')]");
            System.out.println("Calculation Result: " + result.getText());

            Thread.sleep(3000);
            driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] getCalculatorAppDetails() throws Exception {
        // Step 1: Detect installed calculator package
        String packageName = null;
        Process listPkgs = Runtime.getRuntime().exec("adb shell pm list packages | grep -i calculator");
        BufferedReader pkgReader = new BufferedReader(new InputStreamReader(listPkgs.getInputStream()));
        String line;
        while ((line = pkgReader.readLine()) != null) {
            if (line.contains("package:")) {
                packageName = line.replace("package:", "").trim();
                break;
            }
        }
        if (packageName == null) throw new RuntimeException("Calculator app not found on device.");

        // Step 2: Launch the app using ADB monkey
        Runtime.getRuntime().exec("adb shell monkey -p " + packageName + " -c android.intent.category.LAUNCHER 1");
        Thread.sleep(2000); // Wait for the app to come to foreground

        // Step 3: Get the current foreground activity
        Process activityProc = Runtime.getRuntime().exec("adb shell dumpsys window | grep -E 'mCurrentFocus'");
        BufferedReader activityReader = new BufferedReader(new InputStreamReader(activityProc.getInputStream()));
        String activityLine = activityReader.readLine();
        if (activityLine != null && activityLine.contains(packageName)) {
            String fullActivity = activityLine.substring(activityLine.indexOf(packageName));
            String[] parts = fullActivity.split("/");
            String activityName = parts[1].replaceAll("[}\\s]", "");
            return new String[]{packageName, activityName};
        }

        throw new RuntimeException("Unable to determine calculator app's activity.");
    }
}
