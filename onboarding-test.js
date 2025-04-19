const { remote } = require("webdriverio");
const fs = require("fs");
const emailFile = "emailCounter.txt";
const { execSync } = require("child_process");
const inquirer = require("inquirer");

const APP_PACKAGE = "com.parentgeenee";
const APP_ACTIVITY = "io.ionic.starter.activity.MainActivity";

const generateUniqueNumber = () => {
  let counter = 1;
  if (fs.existsSync(emailFile)) {
    counter = parseInt(fs.readFileSync(emailFile, "utf8"), 10) || 1;
  }
  fs.writeFileSync(emailFile, (counter + 1).toString());
  return counter;
};

const askRunCount = async () => {
  const response = await inquirer.prompt([
    {
      type: "input",
      name: "runCount",
      message: "🔄 How many times should the script run? (Default: 1)",
      default: "1",
      validate: (value) => {
        const parsed = parseInt(value, 10);
        return !isNaN(parsed) && parsed > 0
          ? true
          : "❌ Please enter a valid number greater than 0.";
      },
    },
  ]);
  return parseInt(response.runCount, 10);
};

function listAdbDevices() {
  try {
    const output = execSync("adb devices").toString();
    const lines = output.split("\n").slice(1).filter((line) => line.trim() !== "");
    const devices = lines.map((line) => line.split("\t")[0]);

    if (devices.length === 0) {
      console.error("❌ No ADB devices found!");
      process.exit(1);
    }

    return devices;
  } catch (error) {
    console.error("❌ Error fetching ADB devices:", error.message);
    process.exit(1);
  }
}

async function askDeviceSelection(devices) {
  const selectedDevice = await inquirer.prompt([
    {
      type: "list",
      name: "device",
      message: "📱 Select a device:",
      choices: devices,
    },
  ]);
  console.log({ selectedDevice: selectedDevice.device });
  return selectedDevice.device;
}

const runAutomation = async (driver) => {
  const uniqueNumber = generateUniqueNumber();

  try {
    await driver.terminateApp(APP_PACKAGE);
    console.log("✅ App data cleared successfully!");

    await driver.activateApp(APP_PACKAGE);
    console.log("✅ App relaunched successfully!");

    await driver.waitUntil(async () => (await driver.$('//*[@text="Parent"]')).isDisplayed(), { timeout: 30000 });
    await (await driver.$('//*[@text="Parent"]')).click();

    await driver.waitUntil(async () => (await driver.$("android.widget.EditText")).isDisplayed(), { timeout: 30000 });
    await (await driver.$("android.widget.EditText")).setValue(`automail${uniqueNumber}-dev@xforia.com`);

    await driver.waitUntil(async () => (await driver.$("(//android.widget.CheckBox)[1]")).isDisplayed(), { timeout: 30000 });
    await (await driver.$("(//android.widget.CheckBox)[1]")).click();

    await driver.waitUntil(async () => (await driver.$('//android.widget.Button[@text="SEND OTP"]')).isDisplayed(), { timeout: 30000 });
    await (await driver.$('//android.widget.Button[@text="SEND OTP"]')).click();

    await driver.waitUntil(async () => (await driver.$$("android.widget.EditText")).length >= 4, {
      timeout: 30000,
      timeoutMsg: "❌ OTP fields not loaded in time.",
    });

    const otpInputs = await driver.$$("android.widget.EditText");
    if (otpInputs.length >= 4) {
      console.log("✅ Found OTP input fields. Entering OTP using key events...");
      await otpInputs[0].click();

      const otpDigits = ["1", "2", "3", "4"];
      for (const digit of otpDigits) {
        const keyCode = 7 + parseInt(digit);
        await driver.pressKeyCode(keyCode);
        await driver.pause(300);
      }
    } else {
      console.log("❌ OTP input fields not found or insufficient.");
    }

    await driver.waitUntil(async () => (await driver.$("android.widget.EditText")).isDisplayed(), { timeout: 30000 });
    await (await driver.$("android.widget.EditText")).setValue("Auto Parent");

    await driver.waitUntil(async () => (await driver.$('//android.widget.Button[@text="CONTINUE"]')).isDisplayed(), { timeout: 30000 });
    await (await driver.$('//android.widget.Button[@text="CONTINUE"]')).click();

    await driver.waitUntil(
      async () =>
        (
          await driver.$(
            '//android.view.View[@resource-id="root"]/android.view.View/android.view.View/android.view.View/android.view.View/android.view.View/android.view.View/android.view.View[2]/android.view.View/android.widget.EditText'
          )
        ).isDisplayed(),
      { timeout: 30000 }
    );
    await (
      await driver.$(
        '//android.view.View[@resource-id="root"]/android.view.View/android.view.View/android.view.View/android.view.View/android.view.View/android.view.View/android.view.View[2]/android.view.View/android.widget.EditText'
      )
    ).setValue("Xforia technologies");

    await driver.waitUntil(async () => (await driver.$('//*[contains(@text, "XFORIA TECHNOLOGIES")]')).isDisplayed(), { timeout: 30000 });
    await (await driver.$('//*[contains(@text, "XFORIA TECHNOLOGIES")]')).click();

    await driver.waitUntil(async () => (await driver.$("//android.widget.EditText[@hint='Name your space...']")).isDisplayed(), {
      timeout: 30000,
    });
    await (await driver.$("//android.widget.EditText[@hint='Name your space...']")).setValue("Ambal Nagar");

    await driver.waitUntil(async () => (await driver.$("//android.widget.SeekBar")).isDisplayed(), { timeout: 30000 });
    await driver.waitUntil(async () => (await driver.$('//*[contains(@text, "CREATE SPACE")]')).isDisplayed(), { timeout: 30000 });
    await (await driver.$('//*[contains(@text, "CREATE SPACE")]')).click();

    await driver.waitUntil(async () => (await driver.$$("android.widget.ToggleButton")).length >= 3, {
      timeout: 30000,
      timeoutMsg: "❌ Less than 3 toggle buttons loaded!",
    });

    const toggleButtons = await driver.$$("android.widget.ToggleButton");
    for (const toggle of toggleButtons) {
      await toggle.click();
      await driver.pause(300);
    }

    let continueExists = true;
    while (continueExists) {
      try {
        const continueButton = await driver.$('//android.widget.Button[translate(@text, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz")="continue"]');
        const isDisplayed = await continueButton.isDisplayed();
        if (isDisplayed) {
          await continueButton.click();
          await driver.pause(500);
        } else {
          continueExists = false;
        }
      } catch {
        continueExists = false;
      }
    }

    await driver.waitUntil(
      async () =>
        (
          await driver.$(
            '//android.widget.Button[contains(translate(@text, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz"), "enable")]'
          )
        ).isDisplayed(),
      { timeout: 30000 }
    );
    await (
      await driver.$(
        '//android.widget.Button[contains(translate(@text, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz"), "enable")]'
      )
    ).click();

    await driver.waitUntil(
      async () => {
        try {
          return await driver.getAlertText();
        } catch {
          return false;
        }
      },
      { timeout: 10000, timeoutMsg: "❌ Alert did not appear within timeout!" }
    );

    await driver.acceptAlert();

    await driver.waitUntil(async () => (await driver.$('//android.widget.Button[@text="Restore Purchase"]')).isDisplayed(), {
      timeout: 30000,
    });
    await (await driver.$('//android.widget.Button[@text="Restore Purchase"]')).click();

    // Save source for debugging
    const planScreenSource = await driver.getPageSource();
    fs.writeFileSync("choose_plan_screen.xml", planScreenSource);

    // Select Free Plan
    try {
      await driver.waitUntil(async () => (await driver.$('//*[contains(translate(@text, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz"), "free")]')).isDisplayed(), {
        timeout: 30000,
        timeoutMsg: "❌ 'Free' plan option not found!",
      });
      await (await driver.$('//*[contains(translate(@text, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz"), "free")]')).click();
      console.log("✅ Clicked on 'Free' plan option.");

      await driver.waitUntil(async () => (await driver.$('//*[contains(translate(@text, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz"), "start free")]')).isDisplayed(), {
        timeout: 30000,
        timeoutMsg: "❌ 'Start Free' button not found!",
      });
      await (await driver.$('//*[contains(translate(@text, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz"), "start free")]')).click();
      console.log("✅ Clicked on 'Start Free' button.");
    } catch (e) {
      console.error("❌ Could not complete Free plan selection:", e.message);
    }

    await driver.waitUntil(async () => (await driver.$('//*[contains(@text, "Activity Timeline")]')).isDisplayed(), { timeout: 30000 });
  } catch (error) {
    console.error("❌ Error:", error);
  } finally {
    console.log("✅ Successfully completed.");
  }
};

(async () => {
  const devices = listAdbDevices();
  const selectedDevice = await askDeviceSelection(devices);
  const runCount = await askRunCount();
  console.log(`✅ Selected Device: ${selectedDevice}`);

  const APK_PATH = "C:\\Users\\user\\parentgeenee-april-17-qa.apk";

  const opts = {
    protocol: "http",
    hostname: "localhost",
    port: 4723,
    path: "/",
    capabilities: {
      platformName: "Android",
      "appium:automationName": "UiAutomator2",
      "appium:deviceName": selectedDevice,
      "appium:udid": selectedDevice,
      "appium:app": APK_PATH,
      "appium:autoGrantPermissions": true,
      "appium:noReset": false,
    },
  };

  for (let i = 0; i < runCount; i++) {
    console.log(`\n🔄 Running automation attempt ${i + 1}/${runCount}...`);
    if (i !== 0) await new Promise((r) => setTimeout(r, 5000));
    const driver = await remote(opts);
    await runAutomation(driver);
    if (driver) {
      try {
        await driver.deleteSession();
      } catch {
        console.warn("⚠️ Session was already closed or invalid.");
      }
    }
  }

  console.log("🎉 All automation runs completed successfully!");
})();
