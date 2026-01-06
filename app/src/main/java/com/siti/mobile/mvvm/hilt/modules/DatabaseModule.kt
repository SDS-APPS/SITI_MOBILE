package com.siti.mobile.mvvm.hilt.modules

import android.content.Context
import androidx.room.Room
import com.siti.mobile.Utils.RootDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

// Define el módulo para proveer la base de datos
@Module
@InstallIn(SingletonComponent::class) // para que sea singleton en toda la app
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRootDatabase(
        @ApplicationContext context: Context
    ): RootDatabase {
        return Room.databaseBuilder(
            context,
            RootDatabase::class.java,
            "sdsiptvdb"
        )
            .allowMainThreadQueries() // ojo con esto en producción, evita si puedes
            .addMigrations(RootDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }
}