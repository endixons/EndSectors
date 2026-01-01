package pl.endixon.sectors.tools.manager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import pl.endixon.sectors.tools.utils.LoggerUtil;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Getter
public class MongoManager {

    private MongoClient client;
    private MongoDatabase database;

    public void connect(String uri, String databaseName) {
        LoggerUtil.info("Initializing MongoDB connection provider...");

        try {
            CodecRegistry pojoCodecRegistry = fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .codecRegistry(pojoCodecRegistry)
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .applicationName("EndSectors-Tools")
                    .build();

            this.client = MongoClients.create(settings);
            this.database = client.getDatabase(databaseName);
            validateAndCleanup();
            LoggerUtil.info("Successfully established connection to database: {}", databaseName);

        } catch (MongoException e) {
            LoggerUtil.error("--------------------------------------------------");
            LoggerUtil.error("CRITICAL: MongoDB Authentication or Connectivity failure!");
            LoggerUtil.error("Please verify your credentials and network rules.");
            LoggerUtil.error("Details: " + e.getMessage());
            LoggerUtil.error("--------------------------------------------------");
            this.disconnect();
        }
    }

    private void validateAndCleanup() {
        try {
            Document firstDoc = database.getCollection("players").find().first();

            if (firstDoc != null) {
                if (firstDoc.containsKey("content") || !firstDoc.containsKey("name")) {
                    LoggerUtil.warn("Detected corrupted or malicious data in 'players' collection. Executing emergency cleanup...");
                    database.getCollection("players").drop();
                    LoggerUtil.info("Cleanup completed. Database is now ready for fresh data.");
                }
            }
        } catch (Exception e) {
            if (e.getMessage().contains("subtype 3") || e.getMessage().contains("UUID")) {
                LoggerUtil.warn("Incompatible UUID format detected. Purging 'players' collection for compatibility...");
                database.getCollection("players").drop();
            }
        }
    }


    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }
}