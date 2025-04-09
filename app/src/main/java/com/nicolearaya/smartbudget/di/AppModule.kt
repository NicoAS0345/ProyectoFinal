package com.nicole_u_latina_araya_solano.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nicole_u_latina_araya_solano.data.GastosRepository
import com.nicole_u_latina_araya_solano.data.database.AppDatabase
import com.nicole_u_latina_araya_solano.data.database.interfaces.GastosDao
import com.nicolearaya.smartbudget.dataFirebase.GastosRepositoryFirebase
import com.nicolearaya.smartbudget.dataFirebase.Gastos_Firebase
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

    // Configuración de Firebase

    //Proporciona la instancia de Firebase Authentication.
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    //Proporciona la instancia de Firestore con configuración personalizada.

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore.apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
    }

    //Proporciona la clase que maneja las operaciones con gastos en Firestore.
    @Provides
    @Singleton
    fun provideGastosFirebase(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): Gastos_Firebase {
        return Gastos_Firebase(auth, firestore)
    }

    //Proporciona el repositorio de Firebase que abstrae las operaciones con gastos.
    @Provides
    @Singleton
    fun provideGastosRepositoryFirebase(
        gastosFirebase: Gastos_Firebase
    ): GastosRepositoryFirebase {
        return GastosRepositoryFirebase(gastosFirebase)
    }

}