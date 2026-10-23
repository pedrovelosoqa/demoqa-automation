package br.com.desafioqa.api;

import br.com.desafioqa.config.ConfigManager;
import br.com.desafioqa.model.BookSelectionRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class BookStoreApiClient {

    public Response getAvailableBooks() {
        return baseRequest()
                .when()
                .get("/BookStore/v1/Books")
                .then()
                .log().ifValidationFails()
                .extract().response();
    }

    public Response addBooksToUser(BookSelectionRequest request, String token) {
        return baseRequest()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(toJson(request))
                .when()
                .post("/BookStore/v1/Books")
                .then()
                .log().ifValidationFails()
                .extract().response();
    }

    private RequestSpecification baseRequest() {
        RequestSpecification request = given()
                .baseUri(ConfigManager.getApiBaseUrl())
                .accept(ContentType.JSON);
        if (ConfigManager.isApiRelaxedHttps()) {
            request.relaxedHTTPSValidation();
        }
        return request;
    }

    private String toJson(BookSelectionRequest request) {
        StringBuilder json = new StringBuilder("{\"userId\":\"")
                .append(escapeJson(request.getUserId()))
                .append("\",\"collectionOfIsbns\":[");

        for (int index = 0; index < request.getCollectionOfIsbns().size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append("{\"isbn\":\"")
                    .append(escapeJson(request.getCollectionOfIsbns().get(index).getIsbn()))
                    .append("\"}");
        }
        return json.append("]}").toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.toString();
    }
}
