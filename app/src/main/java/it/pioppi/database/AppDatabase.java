package it.pioppi.database;

import static it.pioppi.ConstantUtils.APP_DATABASE;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ProviderEntityDao;
import it.pioppi.database.model.ItemEntity;
import it.pioppi.database.model.ProviderEntity;
import it.pioppi.database.tipeconverters.LocalDateTimeConverter;
import it.pioppi.database.tipeconverters.UUIDConverter;

@Database(entities = {ItemEntity.class, ProviderEntity.class}, version = 1)
@TypeConverters({LocalDateTimeConverter.class, UUIDConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ItemEntityDao itemEntityDao();
    public abstract ProviderEntityDao providerEntityDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, APP_DATABASE)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
