package it.pioppi.database;

import static it.pioppi.ConstantUtils.APP_DATABASE;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.pioppi.database.dao.ItemDetailEntityDao;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.dao.ItemTagEntityDao;
import it.pioppi.database.dao.ProviderEntityDao;
import it.pioppi.database.dao.QuantityTypeEntityDao;
import it.pioppi.database.model.entity.ItemDetailEntity;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemFTSEntity;
import it.pioppi.database.model.entity.ItemTagEntity;
import it.pioppi.database.model.entity.ItemTagJoinEntity;
import it.pioppi.database.model.entity.ProviderEntity;
import it.pioppi.database.model.entity.QuantityTypeEntity;
import it.pioppi.database.typeconverters.Converters;

@Database(entities = {ItemEntity.class, ItemDetailEntity.class, ProviderEntity.class, QuantityTypeEntity.class,
        ItemTagEntity.class, ItemTagJoinEntity.class, ItemFTSEntity.class}, version = 27)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ItemEntityDao itemEntityDao();
    public abstract ProviderEntityDao providerEntityDao();
    public abstract ItemDetailEntityDao itemDetailEntityDao();
    public abstract QuantityTypeEntityDao quantityTypeEntityDao();
    public abstract ItemTagEntityDao itemTagEntityDao();
    public abstract ItemFTSEntityDao itemFTSEntityDao();



    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, APP_DATABASE)
                            .createFromAsset("database/pioppi.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }


}
