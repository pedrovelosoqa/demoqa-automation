package br.com.desafioqa.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page object for Interactions / Sortable. */
public class SortablePage extends BasePage {

    private static final By PAGE_TITLE = By.xpath("//h1[normalize-space()='Sortable']");
    private static final By LIST_TAB = By.id("demo-tab-list");
    private static final By LIST_PANE = By.id("demo-tabpane-list");
    private static final By LIST_ITEMS = By.cssSelector("#demo-tabpane-list .list-group-item");

    private final WebDriverWait pageWait;

    public SortablePage(WebDriver driver) {
        super(driver);
        this.pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isLoaded() {
        return waitUntilVisible(PAGE_TITLE).isDisplayed();
    }

    public void selectListTab() {
        safeClick(LIST_TAB);
        pageWait.withMessage("Sortable list tab did not become active")
                .until(driver -> {
                    String classes = driver.findElement(LIST_TAB).getAttribute("class");
                    return classes != null && classes.contains("active")
                            && driver.findElement(LIST_PANE).isDisplayed();
                });
    }

    public List<String> getListOrder() {
        waitUntilVisible(LIST_PANE);
        return driver.findElements(LIST_ITEMS).stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getText().trim())
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    public void moveItemBefore(String itemText, String targetText) {
        List<String> order = getListOrder();
        int targetIndex = order.indexOf(targetText);
        if (targetIndex < 0) {
            throw new NoSuchElementException("Sortable target item not found: " + targetText);
        }
        moveItemToPosition(itemText, targetIndex);
    }

    public void moveItemToPosition(String itemText, int targetIndex) {
        List<String> before = getListOrder();
        int sourceIndex = before.indexOf(itemText);
        if (sourceIndex < 0) {
            throw new NoSuchElementException("Sortable item not found: " + itemText);
        }
        if (targetIndex < 0 || targetIndex >= before.size()) {
            throw new IllegalArgumentException(
                    "Target index " + targetIndex + " is outside list size " + before.size());
        }
        if (sourceIndex == targetIndex) {
            return;
        }

        try {
            performDragAndDrop(itemText, targetIndex, false);
        } catch (WebDriverException ignored) {
            // Some drivers do not implement the convenience dragAndDrop sequence
            // for React Sortable. The explicit pointer sequence below is the fallback.
        }
        if (!waitForItemAtPosition(itemText, targetIndex, Duration.ofSeconds(2))) {
            performDragAndDrop(itemText, targetIndex, true);
        }

        pageWait.withMessage(() -> "Could not move Sortable item '" + itemText
                        + "' to index " + targetIndex + "; current order: " + getListOrder())
                .ignoring(StaleElementReferenceException.class)
                .until(driver -> itemText.equals(getListOrder().get(targetIndex)));
    }

    /**
     * Reorders through user-like drag operations. The order is read again after every move,
     * so stale element references and shifting indices are not carried between iterations.
     */
    public void arrangeInOrder(List<String> expectedOrder) {
        List<String> current = getListOrder();
        if (current.size() != expectedOrder.size()
                || !current.containsAll(expectedOrder)
                || !expectedOrder.containsAll(current)) {
            throw new IllegalArgumentException(
                    "Expected items do not match the visible Sortable items. Visible="
                            + current + ", expected=" + expectedOrder);
        }

        for (int index = 0; index < expectedOrder.size(); index++) {
            String expectedItem = expectedOrder.get(index);
            current = getListOrder();
            if (!expectedItem.equals(current.get(index))) {
                moveItemToPosition(expectedItem, index);
            }
        }
    }

    public boolean hasExactOrder(List<String> expectedOrder) {
        return getListOrder().equals(expectedOrder);
    }

    private void performDragAndDrop(String itemText, int targetIndex, boolean fallback) {
        List<WebElement> items = visibleListItems();
        WebElement source = findItem(items, itemText);
        WebElement target = items.get(targetIndex);

        Actions actions = new Actions(driver);
        if (!fallback) {
            actions.dragAndDrop(source, target).perform();
            return;
        }

        int sourceIndex = indexOf(items, itemText);
        int verticalOffset = sourceIndex > targetIndex
                ? -Math.max(4, target.getSize().getHeight() / 3)
                : Math.max(4, target.getSize().getHeight() / 3);

        new Actions(driver)
                .moveToElement(source)
                .clickAndHold()
                .pause(Duration.ofMillis(150))
                .moveByOffset(0, sourceIndex > targetIndex ? -4 : 4)
                .moveToElement(target)
                .moveByOffset(0, verticalOffset)
                .pause(Duration.ofMillis(200))
                .release()
                .perform();
    }

    private boolean waitForItemAtPosition(String itemText, int targetIndex, Duration timeout) {
        try {
            return new WebDriverWait(driver, timeout)
                    .ignoring(StaleElementReferenceException.class)
                    .until(driver -> {
                        List<String> current = getListOrder();
                        return current.size() > targetIndex
                                && itemText.equals(current.get(targetIndex));
                    });
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return false;
        }
    }

    private List<WebElement> visibleListItems() {
        List<WebElement> visible = new ArrayList<>();
        for (WebElement item : driver.findElements(LIST_ITEMS)) {
            if (item.isDisplayed()) {
                visible.add(item);
            }
        }
        return Collections.unmodifiableList(visible);
    }

    private WebElement findItem(List<WebElement> items, String itemText) {
        return items.stream()
                .filter(item -> itemText.equals(item.getText().trim()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Sortable item not found: " + itemText));
    }

    private int indexOf(List<WebElement> items, String itemText) {
        for (int index = 0; index < items.size(); index++) {
            if (itemText.equals(items.get(index).getText().trim())) {
                return index;
            }
        }
        throw new NoSuchElementException("Sortable item not found: " + itemText);
    }
}
