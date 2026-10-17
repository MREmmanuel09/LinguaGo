package dev.linguago.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import dev.linguago.core.data.repository.GetNextReviewUseCaseImpl
import dev.linguago.core.data.repository.GradeReviewUseCaseImpl
import dev.linguago.core.data.repository.ProgressRepositoryImpl
import dev.linguago.core.data.repository.ReviewRepositoryImpl
import dev.linguago.core.data.repository.SubmitAnswerUseCaseImpl
import dev.linguago.core.data.repository.UpdateStreakUseCaseImpl
import dev.linguago.core.domain.repository.ProgressRepository
import dev.linguago.core.domain.repository.ReviewRepository
import dev.linguago.core.domain.repository.SettingsRepository
import dev.linguago.core.domain.usecase.CompleteLessonUseCase
import dev.linguago.core.domain.usecase.GetNextReviewUseCase
import dev.linguago.core.domain.usecase.GradeReviewUseCase
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
            .addMigrations(MIGRATION_1_2)
            .build()

    /** v2 (ADR-016): tabla de tarjetas FSRS; migración sin pérdida de datos. */
    private val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `word_progress` (" +
                        "`word` TEXT NOT NULL, " +
                        "`courseId` TEXT NOT NULL, " +
                        "`translation` TEXT NOT NULL, " +
                        "`reviewCount` INTEGER NOT NULL, " +
                        "`lastReviewEpochMillis` INTEGER NOT NULL, " +
                        "`nextReviewEpochMillis` INTEGER NOT NULL, " +
                        "`stability` REAL NOT NULL, " +
                        "`difficulty` REAL NOT NULL, " +
                        "PRIMARY KEY(`word`, `courseId`))",
                )
            }
        }

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
    public abstract fun bindsGetNextReviewUseCase(impl: GetNextReviewUseCaseImpl): GetNextReviewUseCase

    @Binds
    public abstract fun bindsGradeReviewUseCase(impl: GradeReviewUseCaseImpl): GradeReviewUseCase
}
