1. Detect Default Browser

Java cannot natively read the OS default browser reliably across all platforms. But we can use OS-specific commands to detect it:

Windows
String cmd = "REG QUERY HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\Shell\\Associations\\UrlAssociations\\http\\UserChoice /v ProgId";
Process p = Runtime.getRuntime().exec(cmd);
BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
String line;
while ((line = reader.readLine()) != null) {
    if (line.contains("ProgId")) {
        System.out.println("Default browser ProgId: " + line.split("\\s+")[line.split("\\s+").length-1]);
    }
}

macOS
Process p = Runtime.getRuntime().exec("defaults read com.apple.LaunchServices/com.apple.launchservices.secure LSHandlers -array");

Linux

Check $BROWSER environment variable:

String browser = System.getenv("BROWSER");
if (browser != null) {
    System.out.println("Detected browser: " + browser);
}


âš ï¸ These detections are advisory; Selenium still requires a driver for automation.

2. Recommended Approach: Use Chrome/Chromium

Chrome is widely supported, cross-platform, and Seleniumâ€™s ChromeDriver is stable.

Optionally, you can bundle Chromium for headless automation, so you donâ€™t rely on system default browser.

3. Auto-Fallback Logic
public WebDriver createWebDriver() {
    String driverPath = System.getProperty("webdriver.chrome.driver");
    if(driverPath == null || driverPath.isEmpty()) {
        // fallback to bundled chromedriver
        driverPath = "drivers/chromedriver";
    }
    System.setProperty("webdriver.chrome.driver", driverPath);

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--disable-gpu");
    options.addArguments("--no-sandbox");

    return new ChromeDriver(options);
}


If ChromeDriver is missing â†’ throw a descriptive exception:
"ChromeDriver not found. Please install or configure path."

4. Optional: Detect Installed Browsers for Selenium

You can scan typical paths and try drivers dynamically:

OS	Default Locations
Windows	C:\Program Files\Google\Chrome\Application\chrome.exe
macOS	/Applications/Google Chrome.app/Contents/MacOS/Google Chrome
Linux	/usr/bin/google-chrome or /usr/bin/chromium-browser
if(Files.exists(Paths.get(path))) {
    options.setBinary(path);
}

âœ… Recommended Enterprise Pattern

Primary: Use headless Chrome with configurable driver path.

Secondary (optional): Detect system browser binary and set ChromeOptions binary.

Avoid: Full Firefox/Edge support unless strictly required.

This ensures deterministic behavior for Selenium automation in production.