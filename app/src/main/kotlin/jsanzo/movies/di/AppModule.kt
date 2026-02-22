package jsanzo.movies.di

import jsanzo.movies.data.di.DataModule
import jsanzo.movies.database.di.DatabaseModule
import jsanzo.movies.di.remote.AppRemoteModule
import jsanzo.movies.domain.di.DomainModule
import jsanzo.movies.remote.di.NetworkModule
import jsanzo.movies.remote.di.RemoteModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        DataModule::class,
        DomainModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        AppRemoteModule::class,
        RemoteModule::class,
    ],
)
@ComponentScan("jsanzo.movies")
class AppModule
