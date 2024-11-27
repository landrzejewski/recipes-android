package pl.training.recipes

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import pl.training.recipes.adapters.provider.HttpRecipesProvider
import pl.training.recipes.adapters.provider.HttpRecipesProviderAdapter
import pl.training.recipes.adapters.provider.HttpRecipesProviderMapper
import pl.training.recipes.domain.GetRecipesUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RecipesModule {

    @Singleton
    @Provides
    fun httpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = BASIC
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun httpRecipesProvider(httpClient: OkHttpClient): HttpRecipesProvider = Retrofit.Builder()
        .baseUrl("https://dummyjson.com")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
        .create(HttpRecipesProvider::class.java)

    @Singleton
    @Provides
    fun httpRecipesProviderMapper(): HttpRecipesProviderMapper = HttpRecipesProviderMapper()

    @Singleton
    @Provides
    fun httpRecipesProviderAdapter(provider: HttpRecipesProvider, mapper: HttpRecipesProviderMapper) =
        HttpRecipesProviderAdapter(provider, mapper)

    @Singleton
    @Provides
    fun getRecipesUseCase(provider: HttpRecipesProviderAdapter) = GetRecipesUseCase(provider)

}