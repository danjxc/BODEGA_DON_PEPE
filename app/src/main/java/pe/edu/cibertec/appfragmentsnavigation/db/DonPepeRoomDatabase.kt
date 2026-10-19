package pe.edu.cibertec.appfragmentsnavigation.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pe.edu.cibertec.appfragmentsnavigation.db.dao.CarritoDao
import pe.edu.cibertec.appfragmentsnavigation.db.dao.ProductoDao
import pe.edu.cibertec.appfragmentsnavigation.db.dao.VentaPendienteDao
import pe.edu.cibertec.appfragmentsnavigation.db.entity.CarritoItemEntity
import pe.edu.cibertec.appfragmentsnavigation.db.entity.ProductoEntity
import pe.edu.cibertec.appfragmentsnavigation.db.entity.VentaPendienteEntity

@Database(
    entities = [ProductoEntity::class, CarritoItemEntity::class, VentaPendienteEntity::class],
    version = 2
)
abstract class DonPepeRoomDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun carritoDao(): CarritoDao
    abstract fun ventaPendienteDao(): VentaPendienteDao

    companion object {
        @Volatile
        private var INSTANCE: DonPepeRoomDatabase? = null

        fun getDatabase(context: Context): DonPepeRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DonPepeRoomDatabase::class.java,
                    "donpepedb"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}