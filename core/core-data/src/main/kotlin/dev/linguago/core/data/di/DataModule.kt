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
import dev.linguago.core.data.database.DatabaseMigrations
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.data.preferences.SettingsRepositoryImpl
import dev.linguago.core.data.repository.CompleteLessonUseCaseImpl
import dev.linguago.core.data.repository.ConsumeHeartUseCaseImpl
import dev.linguago.core.data.repository.GetNextReviewUseCaseImpl
import dev.linguago.core.data.repository.GradeReviewUseCaseImpl
import dev.linguago.core.data.repository.HeartsRepositoryImpl
import dev.linguago.core.data.repository.ObserveHeartsUseCaseImpl
import dev.linguago.core.data.repository.ProgressRepositoryImpl
import dev.linguago.core.data.repository.RegisterAnswerUseCaseImpl
import dev.linguago.core.data.repository.ReviewRepositoryImpl
import dev.linguago.core.data.repository.SubmitAnswerUseCaseImpl
import dev.linguago.core.data.repository.UpdateStreakUseCaseImpl
import dev.linguago.core.domain.repository.HeartsRepository
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.domain.usecase.CompleteLessonUseCase
import dev.linguago.core.domain.usecase.ConsumeHeartUseCase
import dev.linguago.core.domain.usecase.GetNextReviewUseCase
import dev.linguago.core.domain.usecase.GradeReviewUseCase
import dev.linguago.core.domain.usecase.ObserveHeartsUseCase
import dev.linguago.core.domain.usecase.RegisterAnswerUseCase
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
    ): LinguagoDatabase =
        Room
            .databaseBuilder(context, LinguagoDatabase::class.java, DATABASE_NAME)
            .addMigrations(
                DatabaseMigrations.MIGRATION_1_2,
                DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4,
            ).build()

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

    @Binds
    public abstract fun bindsReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    public abstract fun bindsHeartsRepository(impl: HeartsRepositoryImpl): HeartsRepository

    @Binds
    public abstract fun bindsObserveHeartsUseCase(impl: ObserveHeartsUseCaseImpl): ObserveHeartsUseCase

    @Binds
    public abstract fun bindsConsumeHeartUseCase(impl: ConsumeHeartUseCaseImpl): ConsumeHeartUseCase

    @Binds
    public abstract fun bindsRegisterAnswerUseCase(impl: RegisterAnswerUseCaseImpl): RegisterAnswerUseCase

    @Binds
    public abstract fun bindsGetNextReviewUseCase(impl: GetNextReviewUseCaseImpl): GetNextReviewUseCase

    @Binds
    public abstract fun bindsGradeReviewUseCase(impl: GradeReviewUseCaseImpl): GradeReviewUseCase
}
