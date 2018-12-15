package data;

import data.messages.SearchQueryMessage;

import java.time.LocalDateTime;

public class Subscription {
    private SearchQueryMessage query;
    private String userId;
    private LocalDateTime createdAt;

    public Subscription(SearchQueryMessage query, String userId, LocalDateTime createdAt) {
        this.query = query;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public Subscription() {}

    public SearchQueryMessage getQuery() {
        return query;
    }

    public void setQuery(SearchQueryMessage query) {
        this.query = query;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
