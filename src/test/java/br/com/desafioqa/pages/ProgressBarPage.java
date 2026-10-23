package br.com.desafioqa.pages;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page object for Widgets / Progress Bar. */
public class ProgressBarPage extends BasePage {

    private static final By START_STOP_BUTTON = By.id("startStopButton");
    private static final By RESET_BUTTON = By.id("resetButton");
    private static final By PROGRESS_VALUE =
            By.cssSelector("#progressBar [role='progressbar'], #progressBar .progress-bar");

    private static final Duration PROGRESS_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration POLLING_INTERVAL = Duration.ofMillis(40);
    private static final Duration STOP_POLLING_INTERVAL = Duration.ofMillis(10);
    private static final Duration STOP_STABILITY_WINDOW = Duration.ofMillis(150);

    private final WebDriverWait pageWait;

    public ProgressBarPage(WebDriver driver) {
        super(driver);
        this.pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isLoaded() {
        waitForUrlContains("/progress-bar");
        waitUntilClickable(START_STOP_BUTTON);
        pageWait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(PROGRESS_VALUE));
        return true;
    }

    public int getProgressValue() {
        WebElement progress = pageWait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(PROGRESS_VALUE));
        String ariaValue = progress.getAttribute("aria-valuenow");
        String value = ariaValue == null || ariaValue.trim().isEmpty()
                ? progress.getText()
                : ariaValue;
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            throw new IllegalStateException(
                    "Could not read progress from aria-valuenow or visible text. Raw value: " + value);
        }
        return Integer.parseInt(digits);
    }

    public void start() {
        if (!isRunning()) {
            safeClick(START_STOP_BUTTON);
            pageWait.withMessage("Progress Bar did not start; button never changed to Stop")
                    .until(driver -> isRunning());
        }
    }

    public void stop() {
        if (isRunning()) {
            safeClick(START_STOP_BUTTON);
            pageWait.withMessage("Progress Bar did not stop; button still shows Stop")
                    .until(driver -> !isRunning());
        }
    }

    public boolean isRunning() {
        if (driver.findElements(START_STOP_BUTTON).isEmpty()) {
            return false;
        }
        return "stop".equalsIgnoreCase(driver.findElement(START_STOP_BUTTON).getText().trim());
    }

    /** Stops in the requested interval while compensating for UI click latency. */
    public int startAndStopAtOrAbove(int minimum, int maximumAllowed) {
        if (minimum < 1 || maximumAllowed > 100 || minimum > maximumAllowed) {
            throw new IllegalArgumentException("Invalid progress interval: " + minimum + ".." + maximumAllowed);
        }

        prepareStartStopButton();
        int stopTrigger = Math.max(1, minimum - 3);

        for (int attempt = 1; attempt <= 3; attempt++) {
            clickStartStopImmediately();
            waitAndStopAtTrigger(stopTrigger, maximumAllowed);

            int stableValue = waitUntilStable(4);
            if (stableValue >= minimum && stableValue <= maximumAllowed) {
                return stableValue;
            }
            if (stableValue > maximumAllowed) {
                throw new AssertionError(
                        "Progress stopped above " + maximumAllowed + "%: " + stableValue + "%");
            }

            // If the early trigger stopped below the minimum, resume from that value
            // and use the functional minimum as the next trigger.
            stopTrigger = minimum;
        }

        throw new AssertionError(
                "Progress could not be stopped between " + minimum + "% and "
                        + maximumAllowed + "% after 3 controlled attempts");
    }

    private void prepareStartStopButton() {
        WebElement button = pageWait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(START_STOP_BUTTON));
        if (!button.isDisplayed() || !button.isEnabled()) {
            throw new IllegalStateException("Progress Bar Start/Stop button is not available");
        }
    }

    private void waitAndStopAtTrigger(int trigger, int maximumAllowed) {
        AtomicInteger lastObserved = new AtomicInteger(getProgressValue());
        try {
            stopWait("Progress did not reach the stop trigger " + trigger + "% within "
                            + PROGRESS_TIMEOUT.getSeconds() + " seconds; last value: "
                            + lastObserved + "%")
                    .until(driver -> {
                        int current = getProgressValue();
                        lastObserved.set(current);
                        if (current > maximumAllowed) {
                            throw new AssertionError(
                                    "Progress passed the safe stop limit of " + maximumAllowed
                                            + "%; observed " + current + "%");
                        }
                        if (current >= trigger) {
                            clickStartStopImmediately();
                            return true;
                        }
                        return false;
                    });
        } catch (TimeoutException exception) {
            throw new AssertionError(
                    "Progress did not reach the stop trigger " + trigger
                            + "% before timeout; last observed value was "
                            + lastObserved.get() + "%",
                    exception);
        }
    }

    public int waitUntilStable(int consecutiveEqualReadings) {
        if (consecutiveEqualReadings < 2) {
            throw new IllegalArgumentException("At least two equal readings are required");
        }

        AtomicInteger previous = new AtomicInteger(-1);
        AtomicInteger equalReadings = new AtomicInteger(0);
        AtomicInteger currentValue = new AtomicInteger(getProgressValue());
        AtomicLong unchangedSince = new AtomicLong(System.nanoTime());

        try {
            return stopWait("Progress did not stabilize; last observed value: " + currentValue + "%")
                    .until(driver -> {
                        int current = getProgressValue();
                        currentValue.set(current);
                        if (current == previous.get()) {
                            equalReadings.incrementAndGet();
                        } else {
                            previous.set(current);
                            equalReadings.set(1);
                            unchangedSince.set(System.nanoTime());
                        }

                        long stableNanos = System.nanoTime() - unchangedSince.get();
                        boolean stableLongEnough = stableNanos >= STOP_STABILITY_WINDOW.toNanos();
                        return equalReadings.get() >= consecutiveEqualReadings && stableLongEnough
                                ? current
                                : null;
                    });
        } catch (TimeoutException exception) {
            throw new AssertionError(
                    "Progress did not stabilize before timeout; last observed value was "
                            + currentValue.get() + "%",
                    exception);
        }
    }

    public int resumeAndWaitUntilComplete() {
        start();
        return waitForExactValue(100);
    }

    public int waitForExactValue(int expected) {
        AtomicInteger lastObserved = new AtomicInteger(getProgressValue());
        try {
            return progressWait("Progress did not reach exactly " + expected
                            + "%; last observed value: " + lastObserved + "%")
                    .until(driver -> {
                        int current = getProgressValue();
                        lastObserved.set(current);
                        return current == expected ? current : null;
                    });
        } catch (TimeoutException exception) {
            throw new AssertionError(
                    "Progress did not reach exactly " + expected
                            + "% before timeout; last observed value was " + lastObserved.get() + "%",
                    exception);
        }
    }

    public void reset() {
        pageWait.withMessage("Reset button was not available after Progress Bar completion")
                .until(driver -> !driver.findElements(RESET_BUTTON).isEmpty());
        safeClick(RESET_BUTTON);
        waitForExactValue(0);
    }

    private FluentWait<WebDriver> progressWait(String timeoutMessage) {
        return new FluentWait<>(driver)
                .withTimeout(PROGRESS_TIMEOUT)
                .pollingEvery(POLLING_INTERVAL)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .withMessage(timeoutMessage);
    }

    private FluentWait<WebDriver> stopWait(String timeoutMessage) {
        return new FluentWait<>(driver)
                .withTimeout(PROGRESS_TIMEOUT)
                .pollingEvery(STOP_POLLING_INTERVAL)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .withMessage(timeoutMessage);
    }

    private void clickStartStopImmediately() {
        WebElement button = driver.findElement(START_STOP_BUTTON);
        if (!button.isDisplayed() || !button.isEnabled()) {
            throw new IllegalStateException("Progress Bar Start/Stop button is not available");
        }
        button.click();
    }
}
