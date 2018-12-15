package data.messages;

import data.Subscription;

public class SubscriptionMatchMessage {
    private Subscription subscription;
    private int matchedFileId;

    public SubscriptionMatchMessage(Subscription subscription, int matchedFileId) {
        this.subscription = subscription;
        this.matchedFileId = matchedFileId;
    }

    public SubscriptionMatchMessage() {}

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public int getMatchedFileId() {
        return matchedFileId;
    }

    public void setMatchedFileId(int matchedFileId) {
        this.matchedFileId = matchedFileId;
    }
}
