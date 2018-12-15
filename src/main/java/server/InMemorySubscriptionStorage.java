package server;

import data.Attribute;
import data.MetaData;
import data.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemorySubscriptionStorage implements SubscriptionStorage {

    private ConcurrentLinkedQueue<Subscription> storage = new ConcurrentLinkedQueue<>();

    @Override
    public void put(Subscription subscription) {
        this.storage.offer(subscription);
    }

    @Override
    public List<Subscription> findMatching(MetaData metaData) {
        List<Subscription> result = new ArrayList<>();

        for (Subscription subscription : storage) {
            List<Attribute> attrs = subscription.getQuery().getAttributes();
            try {
                if (metaData.matchAttributes(attrs)) {
                    result.add(subscription);
                }
            } catch (IllegalArgumentException e) {
                // No match, ignore.
            }
        }
        return result;
    }
}
