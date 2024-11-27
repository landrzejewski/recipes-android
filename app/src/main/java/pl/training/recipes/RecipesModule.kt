package pl.training.recipes

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import pl.training.recipes.adapters.persistence.RoomRecipesAdapter
import pl.training.recipes.adapters.persistence.RoomRecipesMapper
import pl.training.recipes.adapters.provider.HttpRecipesProvider
import pl.training.recipes.adapters.provider.HttpRecipesProviderAdapter
import pl.training.recipes.adapters.provider.HttpRecipesProviderMapper
import pl.training.recipes.domain.LoadCachedRecipesUseCase
import pl.training.recipes.domain.RecipesProvider
import pl.training.recipes.domain.RecipesRepository
import pl.training.recipes.domain.RefreshRecipesUseCase
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
    fun httpRecipesProviderAdapter(
        provider: HttpRecipesProvider,
        mapper: HttpRecipesProviderMapper
    ): RecipesProvider =
        HttpRecipesProviderAdapter(provider, mapper)

    @Singleton
    @Provides
    fun database(@ApplicationContext context: Context): RecipesDatabase = Room
        .databaseBuilder(context, RecipesDatabase::class.java, "recipes")
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun roomRecipesMapper(): RoomRecipesMapper = RoomRecipesMapper()

    @Singleton
    @Provides
    fun roomRecipesAdapter(
        database: RecipesDatabase,
        mapper: RoomRecipesMapper
    ): RecipesRepository =
        RoomRecipesAdapter(database.recipesDao(), mapper)

    @Singleton
    @Provides
    fun refreshRecipesUseCase(
        provider: RecipesProvider,
        repository: RecipesRepository
    ): RefreshRecipesUseCase =
        RefreshRecipesUseCase(provider, repository)

    @Singleton
    @Provides
    fun loadCachedRecipesUseCase(repository: RecipesRepository): LoadCachedRecipesUseCase =
        LoadCachedRecipesUseCase(repository)

}