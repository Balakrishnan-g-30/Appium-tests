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
            // Step 1: Dynamically retrieve the calculator app's package and activity name
            String[] appInfo = getCalculatorAppDetails(); // appInfo[0] = package, appInfo[1] = activity

            // Step 2: Set up desired capabilities to connect to the Android device
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("platformName", "Android"); // Target platform
            caps.setCapability("automationName", "UiAutomator2"); // Recommended automation engine
            caps.setCapability("deviceName", "AndroidDevice"); // Placeholder device name

            // Set the calculator app's package and activity
            caps.setCapability("appPackage", appInfo[0]);
            caps.setCapability("appActivity", appInfo[1]);

            // Step 3: Connect to Appium server and initialize AndroidDriver
            URL appiumServerURL = new URL("http://127.0.0.1:4723/wd/hub");
            AndroidDriver<MobileElement> driver = new AndroidDriver<>(appiumServerURL, caps);
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS); // Implicit wait for element loading

            System.out.println("Calculator app is launched. Performing calculation 2 + 3 =");

            // Step 4: Interact with calculator buttons using UiSelector
            driver.findElementByAndroidUIAutomator("new UiSelector().text(\"2\")").click(); // Click "2"
            driver.findElementByAndroidUIAutomator("new UiSelector().descriptionContains(\"plus\")").click(); // Click "+"
            driver.findElementByAndroidUIAutomator("new UiSelector().text(\"3\")").click(); // Click "3"
            driver.findElementByAndroidUIAutomator("new UiSelector().descriptionContains(\"equals\")").click(); // Click "="

            // Step 5: Capture the result using a flexible XPath (for devices with varying resource IDs)
            MobileElement result = driver.findElementByXPath(
                "//*[contains(@resource-id,'result') or contains(@resource-id,'output')]"
            );
            System.out.println("Calculation Result: " + result.getText()); // Print the result (e.g., "5")

            // Step 6: Wait briefly and close the app session
            Thread.sleep(3000);
            driver.quit();
        } catch (Exception e) {
            e.printStackTrace(); // Print any errors during the test
        }
    }

    /**
     * Dynamically identifies the calculator app's package and activity by:
     * 1. Searching installed packages for one that contains "calculator"
     * 2. Launching the app using ADB's monkey command
     * 3. Extracting the current foreground activity name
     */
    public static String[] getCalculatorAppDetails() throws Exception {
        // Step 1: Find package name containing "calculator"
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

        // Step 2: Use ADB monkey to launch the calculator app via its launcher intent
        Runtime.getRuntime().exec("adb shell monkey -p " + packageName + " -c android.intent.category.LAUNCHER 1");
        Thread.sleep(2000); // Wait a bit for the app to come to the foreground

        // Step 3: Use dumpsys to get the current foreground activity
        Process activityProc = Runtime.getRuntime().exec("adb shell dumpsys window | grep -E 'mCurrentFocus'");
        BufferedReader activityReader = new BufferedReader(new InputStreamReader(activityProc.getInputStream()));
        String activityLine = activityReader.readLine();

        // Parse the activity from the output
        if (activityLine != null && activityLine.contains(packageName)) {
            String fullActivity = activityLine.substring(activityLine.indexOf(packageName));
            String[] parts = fullActivity.split("/");
            String activityName = parts[1].replaceAll("[}\\s]", ""); // Remove extra characters
            return new String[]{packageName, activityName}; // Return package and activity
        }

        // If no activity was found, throw an error
        throw new RuntimeException("Unable to determine calculator app's activity.");
    }
}
