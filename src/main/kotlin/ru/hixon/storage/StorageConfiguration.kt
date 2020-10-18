package ru.hixon.storage

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires

@ConfigurationProperties(StorageConfiguration.PREFIX)
@Requires(property = StorageConfiguration.PREFIX)
public class StorageConfiguration {
    public lateinit var databaseName: String

    public companion object {
        public const val PREFIX: String = "storage"
    }
}
