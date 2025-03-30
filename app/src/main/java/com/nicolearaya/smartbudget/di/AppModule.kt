package com.nicole_u_latina_araya_solano.di

import android.content.Context
import com.nicole_u_latina_araya_solano.data.GastosRepository
import com.nicole_u_latina_araya_solano.data.database.AppDatabase
import com.nicole_u_latina_araya_solano.data.database.interfaces.GastosDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Este modulo permite poveer las dependencias
@Module
@InstallIn(SingletonComponent::class)//Las dependencias se relacionan con el singleton de la base de datos
object AppModule {

    //Aqui es donde se obtiene la base de datos
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    //Aqui se genera una instancia del DAO para obtener los metodos para que funcione el crud
    @Provides
    @Singleton
    fun provideItemDao(appDatabase: AppDatabase): GastosDao {
        return appDatabase.GastosDao()
    }

    //Aqui se hace una instancia del repositorio que complementa los metodos del DAO
    @Provides
    @Singleton
    fun provideItemRepository(gastosDao: GastosDao): GastosRepository {
        return GastosRepository(gastosDao)
    }

}