package server;

import data.MetaData;
import data.Subscription;

import java.util.List;

public interface SubscriptionStorage {

    void put(Subscription subscription);

    List<Subscription> findMatching(MetaData metaData);
}
