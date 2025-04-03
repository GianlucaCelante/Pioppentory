package it.pioppi.database;

import static it.pioppi.utils.ConstantUtils.APP_DATABASE;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.pioppi.database.dao.ItemDetailEntityDao;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.dao.ItemHistoryEntityDao;
import it.pioppi.database.dao.ItemTagEntityDao;
import it.pioppi.database.dao.ItemTagJoinEntityDao;
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
import it.pioppi.utils.ConstantUtils;

@Database(entities = {ItemEntity.class, ItemDetailEntity.class, ProviderEntity.class, QuantityTypeEntity.class,
        ItemTagEntity.class, ItemTagJoinEntity.class, ItemFTSEntity.class, ItemHistoryEntity.class},
        version = 410)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ItemEntityDao itemEntityDao();
    public abstract ProviderEntityDao providerEntityDao();
    public abstract ItemDetailEntityDao itemDetailEntityDao();
    public abstract QuantityTypeEntityDao quantityTypeEntityDao();
    public abstract ItemTagEntityDao itemTagEntityDao();
    public abstract ItemFTSEntityDao itemFTSEntityDao();
    public abstract ItemHistoryEntityDao itemHistoryEntityDao();
    public abstract ItemTagJoinEntityDao itemTagJoinDao();



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
                copyDatabaseFromAssets(context);
            }
        }
        return INSTANCE;
    }

    protected static void copyDatabaseFromAssets(Context context) {
        String dbName = ConstantUtils.DB_NAME + ".db";
        File dbFile = context.getDatabasePath(dbName);
        if (!dbFile.exists()) {
            try (InputStream is = context.getAssets().open("database/" + dbName);
                 FileOutputStream fos = new FileOutputStream(dbFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                Log.d("DatabaseCopy", "Database copiato con successo");
            } catch (IOException e) {
                Log.e("DatabaseCopy", "Errore nella copia del database", e);
            }
        }
    }

}
