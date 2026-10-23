package br.com.desafioqa.steps;

import br.com.desafioqa.api.AccountApiClient;
import br.com.desafioqa.api.BookStoreApiClient;
import br.com.desafioqa.model.BookSelectionRequest;
import br.com.desafioqa.model.UserCredentials;
import br.com.desafioqa.util.RandomDataGenerator;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiBookStoreSteps {

    private final AccountApiClient accountApiClient = new AccountApiClient();
    private final BookStoreApiClient bookStoreApiClient = new BookStoreApiClient();

    private UserCredentials credentials;
    private String userId;
    private String token;
    private Response createUserResponse;
    private Response tokenResponse;
    private Response authorizationResponse;
    private Response availableBooksResponse;
    private Response addBooksResponse;
    private Response userResponse;
    private final Map<String, String> selectedBooks = new LinkedHashMap<>();

    @Dado("que gero credenciais válidas e dinâmicas para um novo usuário")
    public void generateDynamicCredentials() {
        credentials = new UserCredentials(
                RandomDataGenerator.generateUsername(),
                RandomDataGenerator.generatePassword());
    }

    @Quando("crio o usuário pela API")
    public void createUser() {
        createUserResponse = accountApiClient.createUser(credentials);
    }

    @Entao("a API deve criar o usuário com status 201, identificador, nome e lista de livros vazia")
    public void validateCreatedUser() {
        assertEquals(201, createUserResponse.statusCode(), "Status inesperado ao criar o usuário");

        userId = createUserResponse.jsonPath().getString("userID");
        String returnedUserName = createUserResponse.jsonPath().getString("username");
        List<?> books = createUserResponse.jsonPath().getList("books");

        assertNotNull(userId, "A API não retornou o identificador do usuário");
        assertFalse(userId.isBlank(), "O identificador do usuário está vazio");
        assertEquals(credentials.getUserName(), returnedUserName, "O nome retornado difere do enviado");
        assertNotNull(books, "A lista de livros não foi retornada");
        assertTrue(books.isEmpty(), "Um usuário novo não deve possuir livros");
    }

    @Quando("gero um token para o usuário criado")
    public void generateToken() {
        tokenResponse = accountApiClient.generateToken(credentials);
    }

    @Entao("a API deve retornar status 200, token preenchido e status Success")
    public void validateGeneratedToken() {
        assertEquals(200, tokenResponse.statusCode(), "Status inesperado ao gerar o token");

        token = tokenResponse.jsonPath().getString("token");
        String status = tokenResponse.jsonPath().getString("status");

        assertNotNull(token, "A API não retornou o token");
        assertFalse(token.isBlank(), "O token retornado está vazio");
        assertEquals("Success", status, "A geração do token não foi bem-sucedida");
    }

    @Quando("consulto a autorização do usuário")
    public void checkAuthorization() {
        authorizationResponse = accountApiClient.authorize(credentials);
    }

    @Entao("a API deve retornar status 200 e autorização verdadeira")
    public void validateAuthorization() {
        assertEquals(200, authorizationResponse.statusCode(), "Status inesperado ao autorizar o usuário");
        assertTrue(Boolean.parseBoolean(authorizationResponse.asString()), "O usuário não foi autorizado");
    }

    @Quando("consulto os livros disponíveis na Book Store")
    public void getAvailableBooks() {
        availableBooksResponse = bookStoreApiClient.getAvailableBooks();
    }

    @Entao("a API deve retornar status 200 e pelo menos dois livros válidos e distintos")
    public void validateAndSelectAvailableBooks() {
        assertEquals(200, availableBooksResponse.statusCode(), "Status inesperado ao consultar os livros");

        List<Map<String, Object>> books = availableBooksResponse.jsonPath().getList("books");
        assertNotNull(books, "A API não retornou a lista de livros");

        selectedBooks.clear();
        for (Map<String, Object> book : books) {
            Object isbnValue = book.get("isbn");
            Object titleValue = book.get("title");
            String isbn = isbnValue == null ? "" : isbnValue.toString().trim();
            String title = titleValue == null ? "" : titleValue.toString().trim();

            if (!isbn.isEmpty() && !title.isEmpty()) {
                selectedBooks.putIfAbsent(isbn, title);
            }
            if (selectedBooks.size() == 2) {
                break;
            }
        }

        assertEquals(2, selectedBooks.size(), "A API deve disponibilizar ao menos dois livros válidos e distintos");
    }

    @Quando("adiciono os dois livros selecionados à coleção do usuário")
    public void addSelectedBooks() {
        BookSelectionRequest request = new BookSelectionRequest(userId, new ArrayList<>(selectedBooks.keySet()));
        addBooksResponse = bookStoreApiClient.addBooksToUser(request, token);
    }

    @Entao("a API deve adicionar a coleção com status 201")
    public void validateAddedBooks() {
        assertEquals(201, addBooksResponse.statusCode(), "Status inesperado ao adicionar os livros");
    }

    @Quando("consulto o usuário autenticado pelo identificador")
    public void getAuthenticatedUser() {
        userResponse = accountApiClient.getUser(userId, token);
    }

    @Entao("a API deve retornar o usuário correto com exatamente os dois livros selecionados")
    public void validateUserAndSelectedBooks() {
        assertEquals(200, userResponse.statusCode(), "Status inesperado ao consultar o usuário");
        assertEquals(userId, userResponse.jsonPath().getString("userId"), "Identificador de usuário inesperado");
        assertEquals(credentials.getUserName(), userResponse.jsonPath().getString("username"),
                "Nome de usuário inesperado");

        List<Map<String, Object>> returnedBooks = userResponse.jsonPath().getList("books");
        assertNotNull(returnedBooks, "A API não retornou os livros do usuário");
        assertEquals(2, returnedBooks.size(), "O usuário deve possuir exatamente dois livros");

        Set<String> returnedIsbns = new HashSet<>();
        Map<String, String> returnedTitlesByIsbn = new LinkedHashMap<>();
        for (Map<String, Object> book : returnedBooks) {
            String isbn = String.valueOf(book.get("isbn"));
            String title = String.valueOf(book.get("title"));
            returnedIsbns.add(isbn);
            returnedTitlesByIsbn.put(isbn, title);
        }

        assertEquals(selectedBooks.keySet(), returnedIsbns, "Os ISBNs retornados diferem dos selecionados");
        for (Map.Entry<String, String> selectedBook : selectedBooks.entrySet()) {
            assertEquals(selectedBook.getValue(), returnedTitlesByIsbn.get(selectedBook.getKey()),
                    "Título inesperado para o ISBN " + selectedBook.getKey());
        }
    }
}
