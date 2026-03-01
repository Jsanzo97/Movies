@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package jsanzo.movies.data.model

import jsanzo.movies.domain.model.MinVersionDomainConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinVersionDataConfig(
    @SerialName("minVersion")
    val minVersion: String,
)

fun MinVersionDataConfig.toDomain() = MinVersionDomainConfig(minVersion = minVersion)
