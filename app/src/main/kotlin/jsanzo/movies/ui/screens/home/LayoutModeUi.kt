package jsanzo.movies.ui.screens.home

import jsanzo.movies.domain.model.DomainLayoutModePreference

internal enum class LayoutModeUi(val columns: Int) {
    Grid2(2),
    Grid3(3),
    Grid4(4),
}

internal fun LayoutModeUi.toDomainLayoutModePreference() = when (this) {
    LayoutModeUi.Grid2 -> DomainLayoutModePreference.Grid2
    LayoutModeUi.Grid3 -> DomainLayoutModePreference.Grid3
    LayoutModeUi.Grid4 -> DomainLayoutModePreference.Grid4
}

internal fun DomainLayoutModePreference.toUi() = when (this) {
    DomainLayoutModePreference.Grid2 -> LayoutModeUi.Grid2
    DomainLayoutModePreference.Grid3 -> LayoutModeUi.Grid3
    DomainLayoutModePreference.Grid4 -> LayoutModeUi.Grid4
}
