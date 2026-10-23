package br.com.desafioqa.api;

import br.com.desafioqa.config.ConfigManager;
import br.com.desafioqa.model.UserCredentials;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class AccountApiClient {

    public Response createUser(UserCredentials credentials) {
        return baseRequest()
                .contentType(ContentType.JSON)
                .body(toJson(credentials))
                .when()
                .post("/Account/v1/User")
                .then()
                .log().ifValidationFails()
                .extract().response();
    }

    public Response generateToken(UserCredentials credentials) {
        return baseRequest()
                .contentType(ContentType.JSON)
                .body(toJson(credentials))
                .when()
                .post("/Account/v1/GenerateToken")
                .then()
                .log().ifValidationFails(LogDetail.STATUS)
                .extract().response();
    }

    public Response authorize(UserCredentials credentials) {
        return baseRequest()
                .contentType(ContentType.JSON)
                .body(toJson(credentials))
                .when()
                .post("/Account/v1/Authorized")
                .then()
                .log().ifValidationFails()
                .extract().response();
    }

    public Response getUser(String userId, String token) {
        return baseRequest()
                .auth().oauth2(token)
                .pathParam("userId", userId)
                .when()
                .get("/Account/v1/User/{userId}")
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

    private String toJson(UserCredentials credentials) {
        return "{\"userName\":\"" + escapeJson(credentials.getUserName())
                + "\",\"password\":\"" + escapeJson(credentials.getPassword()) + "\"}";
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
