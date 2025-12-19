package pl.endixon.sectors.tools.service;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.*;

@Getter
public class MongoService {

    private MongoClient client;
    private MongoDatabase database;

    public void connect(String uri, String databaseName) {
        var pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        client = MongoClients.create(uri);
        database = client.getDatabase(databaseName)
                .withCodecRegistry(pojoCodecRegistry);
    }

    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }
}
