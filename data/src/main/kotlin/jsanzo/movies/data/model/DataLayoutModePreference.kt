package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainLayoutModePreference

enum class DataLayoutModePreference {
    Grid2,
    Grid3,
    Grid4,
}

fun DataLayoutModePreference.toDomain() = when (this) {
    DataLayoutModePreference.Grid2 -> DomainLayoutModePreference.Grid2
    DataLayoutModePreference.Grid3 -> DomainLayoutModePreference.Grid3
    DataLayoutModePreference.Grid4 -> DomainLayoutModePreference.Grid4
}

fun DomainLayoutModePreference.toData() = when (this) {
    DomainLayoutModePreference.Grid2 -> DataLayoutModePreference.Grid2
    DomainLayoutModePreference.Grid3 -> DataLayoutModePreference.Grid3
    DomainLayoutModePreference.Grid4 -> DataLayoutModePreference.Grid4
}
