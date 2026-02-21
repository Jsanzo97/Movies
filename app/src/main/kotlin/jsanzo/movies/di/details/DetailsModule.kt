package jsanzo.movies.di.details

import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import jsanzo.movies.ui.details.DetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val detailsModule = module {

    viewModel { DetailsViewModel(get()) }

    factory { GetMovieDetailsUseCase(get()) }
}
