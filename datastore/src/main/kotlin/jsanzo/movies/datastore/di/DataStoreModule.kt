package jsanzo.movies.datastore.di

import android.app.Application
import jsanzo.movies.data.datastore.DataStoreStorage
import jsanzo.movies.datastore.DataStore
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class DataStoreModule {

    @Single
    fun provideDataStoreStorage(context: Application): DataStoreStorage = DataStore(context)
}
