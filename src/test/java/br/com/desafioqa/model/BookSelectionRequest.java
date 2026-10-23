package br.com.desafioqa.model;

import java.util.ArrayList;
import java.util.List;

public class BookSelectionRequest {

    private String userId;
    private List<IsbnSelection> collectionOfIsbns;

    public BookSelectionRequest() {
        this.collectionOfIsbns = new ArrayList<>();
    }

    public BookSelectionRequest(String userId, List<String> isbns) {
        this.userId = userId;
        this.collectionOfIsbns = new ArrayList<>();
        for (String isbn : isbns) {
            this.collectionOfIsbns.add(new IsbnSelection(isbn));
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<IsbnSelection> getCollectionOfIsbns() {
        return collectionOfIsbns;
    }

    public void setCollectionOfIsbns(List<IsbnSelection> collectionOfIsbns) {
        this.collectionOfIsbns = collectionOfIsbns;
    }

    public static class IsbnSelection {

        private String isbn;

        public IsbnSelection() {
        }

        public IsbnSelection(String isbn) {
            this.isbn = isbn;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }
    }
}
