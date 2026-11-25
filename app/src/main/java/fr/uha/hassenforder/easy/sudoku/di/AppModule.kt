package fr.uha.hassenforder.easy.sudoku.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.uha.hassenforder.easy.android.navigation.Navigator
import fr.uha.hassenforder.easy.android.navigation.NavigatorType
import fr.uha.hassenforder.easy.sudoku.database.SudokuDatabase
import fr.uha.hassenforder.easy.sudoku.database.dao.GameDao
import fr.uha.hassenforder.easy.sudoku.database.dao.PuzzleDao
import fr.uha.hassenforder.easy.sudoku.navigation.GreetingDestination
import fr.uha.hassenforder.easy.sudoku.navigation.rootNavGraph
import fr.uha.hassenforder.easy.sudoku.repository.GameRepository
import fr.uha.hassenforder.easy.sudoku.repository.PuzzleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Singleton
    @Provides
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) = SudokuDatabase.create(appContext)

    @Singleton
    @Provides
    fun providePuzzleDao(db: SudokuDatabase) = db.getPuzzleDao()

    @Singleton
    @Provides
    fun provideGameDao(db: SudokuDatabase) = db.getGameDao()

    @Singleton
    @Provides
    fun provideNavigator() : Navigator = Navigator(
        startDestination = GreetingDestination.GreetingScreen,
        type = NavigatorType.SINGLETOP,
        navigationGraph = { rootNavGraph() }
    )

    @Singleton
    @Provides
    fun providePuzzleRepository(
        @ApplicationContext appContext: Context,
        ioDispatcher: CoroutineDispatcher,
        puzzleDao: PuzzleDao,
    ) = PuzzleRepository(appContext, ioDispatcher, puzzleDao)

    @Singleton
    @Provides
    fun provideGameRepository(
        @ApplicationContext appContext: Context,
        ioDispatcher: CoroutineDispatcher,
        gameDao: GameDao,
    ) = GameRepository(appContext, ioDispatcher, gameDao)

}