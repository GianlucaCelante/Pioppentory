package it.pioppi.database;

import static it.pioppi.ConstantUtils.APP_DATABASE;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import it.pioppi.database.dao.ItemDetailEntityDao;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.dao.ItemHistoryEntityDao;
import it.pioppi.database.dao.ItemTagEntityDao;
import it.pioppi.database.dao.ProviderEntityDao;
import it.pioppi.database.dao.QuantityTypeEntityDao;
import it.pioppi.database.entity.ItemDetailEntity;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.entity.ItemFTSEntity;
import it.pioppi.database.entity.ItemHistoryEntity;
import it.pioppi.database.entity.ItemTagEntity;
import it.pioppi.database.entity.ItemTagJoinEntity;
import it.pioppi.database.entity.ProviderEntity;
import it.pioppi.database.entity.QuantityTypeEntity;
import it.pioppi.database.typeconverters.Converters;

@Database(entities = {ItemEntity.class, ItemDetailEntity.class, ProviderEntity.class, QuantityTypeEntity.class,
        ItemTagEntity.class, ItemTagJoinEntity.class, ItemFTSEntity.class, ItemHistoryEntity.class},
        version = 34)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ItemEntityDao itemEntityDao();
    public abstract ProviderEntityDao providerEntityDao();
    public abstract ItemDetailEntityDao itemDetailEntityDao();
    public abstract QuantityTypeEntityDao quantityTypeEntityDao();
    public abstract ItemTagEntityDao itemTagEntityDao();
    public abstract ItemFTSEntityDao itemFTSEntityDao();
    public abstract ItemHistoryEntityDao itemHistoryEntityDao();



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
