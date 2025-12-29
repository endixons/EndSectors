/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.tools.manager;

import static org.bson.codecs.configuration.CodecRegistries.*;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.codecs.pojo.PojoCodecProvider;

@Getter
public class MongoManager {

    private MongoClient client;
    private MongoDatabase database;

    public void connect(String uri, String databaseName) {
        var pojoCodecRegistry = fromRegistries(MongoClientSettings
            .getDefaultCodecRegistry(),fromProviders(PojoCodecProvider
            .builder()
            .automatic(true)
            .build())
        );

        client = MongoClients.create(uri);
        database = client.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
    }

    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }
}
