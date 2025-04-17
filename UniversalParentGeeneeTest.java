package appiumtest;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class UniversalParentGeeneeTest {

    public static void main(String[] args) {
        try {
            String[] appInfo = getParentGeeneeAppDetails(); // [0] = package, [1] = activity

            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("platformName", "Android");
            caps.setCapability("automationName", "UiAutomator2");
            caps.setCapability("deviceName", "AndroidDevice");

            // App already installed – no reset, no pull
            caps.setCapability("noReset", true);
            caps.setCapability("fullReset", false);
            caps.setCapability("skipDeviceInitialization", true);
            caps.setCapability("dontStopAppOnReset", true);

            // App-specific caps
            caps.setCapability("appPackage", appInfo[0]);
            caps.setCapability("appActivity", appInfo[1]);
            caps.setCapability("appWaitPackage", appInfo[0]);
            caps.setCapability("appWaitActivity", "*");

            // Critical: Prevent adb pull failures for split APKs
            caps.setCapability("adbExecTimeout", 30000);

            URL appiumServerURL = new URL("http://127.0.0.1:4723/wd/hub");
            AndroidDriver<MobileElement> driver = new AndroidDriver<>(appiumServerURL, caps);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            System.out.println("Parent Geenee app launched. Looking for 'Parent' button...");

            try {
                driver.findElementByAndroidUIAutomator("new UiSelector().text(\"Parent\")").click();
            } catch (Exception e1) {
                try {
                    driver.findElementByAccessibilityId("Parent").click();
                } catch (Exception e2) {
                    throw new Exception("Could not find 'Parent' button by text or accessibility id.");
                }
            }

            System.out.println("'Parent' button clicked successfully.");
            Thread.sleep(3000);
            driver.quit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] getParentGeeneeAppDetails() throws Exception {
        // Step 1: Look for installed package
        String packageName = null;
        Process listPkgs = Runtime.getRuntime().exec("adb shell pm list packages");
        BufferedReader pkgReader = new BufferedReader(new InputStreamReader(listPkgs.getInputStream()));
        String line;
        while ((line = pkgReader.readLine()) != null) {
            if (line.toLowerCase().contains("parentgeenee")) {
                packageName = line.replace("package:", "").trim();
                break;
            }
        }
        if (packageName == null) throw new RuntimeException("Parent Geenee app not found on device.");

        // Step 2: Launch the app with monkey tool
        Runtime.getRuntime().exec("adb shell monkey -p " + packageName + " -c android.intent.category.LAUNCHER 1");
        Thread.sleep(2000);

        // Step 3: Get the top activity
        Process activityProc = Runtime.getRuntime().exec("adb shell dumpsys window windows");
        BufferedReader activityReader = new BufferedReader(new InputStreamReader(activityProc.getInputStream()));
        String activityLine;
        String activityName = null;
        while ((activityLine = activityReader.readLine()) != null) {
            if (activityLine.contains("mCurrentFocus") && activityLine.contains(packageName)) {
                String[] parts = activityLine.substring(activityLine.indexOf(packageName)).split("/");
                activityName = parts[1].replaceAll("[}\\s]", "");
                break;
            }
        }

        if (activityName == null) throw new RuntimeException("Unable to detect app activity for Parent Geenee.");
        return new String[]{packageName, activityName};
    }
}
