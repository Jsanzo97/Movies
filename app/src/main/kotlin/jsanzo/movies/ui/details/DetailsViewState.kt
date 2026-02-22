package jsanzo.movies.ui.details

import jsanzo.movies.domain.model.DomainMovieDetails

sealed class DetailsViewState

object InitialState : DetailsViewState()
object RetrievingDetails : DetailsViewState()
class DetailsRetrieved(val domainMovieDetails: DomainMovieDetails) : DetailsViewState()
class ErrorInOperation(val message: String) : DetailsViewState()
