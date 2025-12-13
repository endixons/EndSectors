package pl.endixon.sectors.common.redis;

import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoManager {

    private static MongoManager instance;

    private final com.mongodb.MongoClient client;
    private final MongoCollection<Document> users;

    private MongoManager() {
        MongoClientOptions options = MongoClientOptions.builder()
                .connectionsPerHost(2)
                .maxWaitTime(10000)
                .build();

        this.client = new com.mongodb.MongoClient(new ServerAddress("localhost", 27017), options);
        MongoDatabase db = client.getDatabase("endsectors");
        this.users = db.getCollection("users");
    }



    public static MongoManager getInstance() {
        if (instance == null) {
            synchronized (MongoManager.class) {
                if (instance == null) {
                    instance = new MongoManager();
                }
            }
        }
        return instance;
    }

    public MongoCollection<Document> getUsersCollection() {
        return this.users;
    }

    public void shutdown() {
        if (client != null) {
            client.close();
        }
    }
}
