package jsanzo.movies.di.home

import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.ui.home.HomeViewModel
import jsanzo.movies.ui.main.NavigationManagerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {

    viewModel { HomeViewModel(get(), get()) }
    viewModel { NavigationManagerViewModel() }

    factory { GetMoviesUseCase(get()) }
    factory { SaveMovieUseCase(get()) }
}