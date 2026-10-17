package dev.linguago.core.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.linguago.core.common.DefaultDispatcherProvider
import dev.linguago.core.common.DispatcherProvider
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.data.preferences.SettingsRepositoryImpl
import dev.linguago.core.data.repository.CompleteLessonUseCaseImpl
import dev.linguago.core.data.repository.ProgressRepositoryImpl
import dev.linguago.core.data.repository.SubmitAnswerUseCaseImpl
import dev.linguago.core.data.repository.UpdateStreakUseCaseImpl
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.domain.usecase.CompleteLessonUseCase
import dev.linguago.core.domain.usecase.SubmitAnswerUseCase
import dev.linguago.core.domain.usecase.UpdateStreakUseCase
import javax.inject.Singleton

/**
 * Módulo Hilt de data: base de datos Room singleton + repositorios de
 * progreso/preferencias + los use cases que orquestan escritura (ADR-005).
 * También publica el único binding de DispatcherProvider para toda la
 * gráfica (lo comparten core-content y core-data).
 */
@Module
@InstallIn(SingletonComponent::class)
public object DataModule {
    @Provides
    @Singleton
    public fun providesLinguagoDatabase(
        @ApplicationContext context: Context,
    ): LinguagoDatabase = Room.databaseBuilder(context, LinguagoDatabase::class.java, DATABASE_NAME).build()

    @Provides
    @Singleton
    public fun providesDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    private const val DATABASE_NAME = "linguago.db"
}

@Module
@InstallIn(SingletonComponent::class)
public abstract class DataRepositoryModule {
    @Binds
    public abstract fun bindsProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    public abstract fun bindsSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    public abstract fun bindsSubmitAnswerUseCase(impl: SubmitAnswerUseCaseImpl): SubmitAnswerUseCase

    @Binds
    public abstract fun bindsCompleteLessonUseCase(impl: CompleteLessonUseCaseImpl): CompleteLessonUseCase

    @Binds
    public abstract fun bindsUpdateStreakUseCase(impl: UpdateStreakUseCaseImpl): UpdateStreakUseCase
}
