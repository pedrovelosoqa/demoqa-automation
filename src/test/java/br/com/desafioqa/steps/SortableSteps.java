package br.com.desafioqa.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.desafioqa.driver.DriverManager;
import br.com.desafioqa.pages.HomePage;
import br.com.desafioqa.pages.SortablePage;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import java.util.Arrays;
import java.util.List;

public class SortableSteps {

    private static final List<String> EXPECTED_ORDER =
            Arrays.asList("One", "Two", "Three", "Four", "Five", "Six");

    private SortablePage sortablePage;
    private List<String> disorderedItems;

    @Dado("que acesso a lista da funcionalidade Sortable")
    public void accessSortableList() {
        HomePage homePage = new HomePage(DriverManager.getDriver());
        homePage.open();
        homePage.selectCard("Interactions");
        homePage.selectSubmenu("Sortable");

        sortablePage = new SortablePage(DriverManager.getDriver());
        assertTrue(sortablePage.isLoaded(), "Sortable page should be loaded");
        sortablePage.selectListTab();
    }

    @Entao("a lista Sortable deve exibir os textos One até Six")
    public void listMustShowExpectedTexts() {
        List<String> actual = sortablePage.getListOrder();
        assertEquals(EXPECTED_ORDER.size(), actual.size(), "Unexpected number of Sortable items");
        assertTrue(actual.containsAll(EXPECTED_ORDER), "Sortable list has unexpected texts: " + actual);
    }

    @Quando("desordeno a lista Sortable de forma controlada")
    public void disorderListInControlledWay() {
        List<String> current = sortablePage.getListOrder();
        if (current.equals(EXPECTED_ORDER)) {
            sortablePage.moveItemToPosition("Six", 0);
        }
        disorderedItems = sortablePage.getListOrder();
        assertNotEquals(EXPECTED_ORDER, disorderedItems, "Sortable list should be out of order");
    }

    @Entao("a ordem da lista deve ser diferente da sequência esperada")
    public void listMustBeOutOfOrder() {
        assertNotEquals(EXPECTED_ORDER, disorderedItems, "Controlled drag should change the order");
        assertEquals(
                disorderedItems,
                sortablePage.getListOrder(),
                "The disordered state should remain visible");
    }

    @Quando("reordeno todos os itens da lista por drag and drop")
    public void reorderEveryItemUsingDragAndDrop() {
        sortablePage.arrangeInOrder(EXPECTED_ORDER);
    }

    @Entao("a lista Sortable deve ficar na sequência One até Six")
    public void listMustMatchExpectedOrder() {
        assertEquals(
                EXPECTED_ORDER,
                sortablePage.getListOrder(),
                "Sortable items should be restored to the expected order");
    }
}
