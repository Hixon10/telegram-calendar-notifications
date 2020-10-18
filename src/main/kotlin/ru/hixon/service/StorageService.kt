package ru.hixon.service

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import org.bson.Document
import org.bson.conversions.Bson
import org.slf4j.LoggerFactory
import ru.hixon.storage.StorageConfiguration
import javax.annotation.PostConstruct
import javax.inject.Singleton


@Singleton
public class StorageService(
        private val mongoClient: MongoClient,
        private val storageConfiguration: StorageConfiguration
) {
    private val logger = LoggerFactory.getLogger(StorageService::class.java)

    private val offsetId = 1L;
    private val telegramOffsetFilter = Filters.eq("_id", offsetId)

    private lateinit var telegramOffsetCollection: MongoCollection<Document>

    @PostConstruct
    public fun init() {
        logger.info("Storage service started")

        telegramOffsetCollection = mongoClient
                .getDatabase(storageConfiguration.databaseName)
                .getCollection("telegramOffset")
    }

    public fun saveTelegramOffset(offset: Long) {
        val update: Bson = Document("\$set",
                Document()
                        .append("_id", offsetId)
                        .append("offsetValue", offset))

        telegramOffsetCollection
                .updateOne(telegramOffsetFilter, update, UpdateOptions().upsert(true))
    }

    public fun getTelegramOffset(): Long? {
        val result = telegramOffsetCollection.find(telegramOffsetFilter).first()
        if (result == null) {
            return null;
        }
        return result.getLong("offsetValue");
    }
}
