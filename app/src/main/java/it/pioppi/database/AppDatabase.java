package it.pioppi.database;

import static it.pioppi.utils.ConstantUtils.APP_DATABASE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
        version = 412)
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
                            //.addMigrations(MIGRATION_410_411, MIGRATION_411_412)
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

    public static final Migration MIGRATION_410_411 = new Migration(410, 411) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE ITEM_HISTORY " +
                            "ADD COLUMN quantity_and_description_and_units TEXT DEFAULT ''"
            );
        }
    };

    public static final Migration MIGRATION_411_412 = new Migration(411, 412) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // 1) Disabilito i vincoli
            db.execSQL("PRAGMA foreign_keys=OFF;");

            // 2) Creo la nuova tabella senza `quantity_and_description_and_units` e con `item_id`
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `item_history_new` (" +
                            "  `id` TEXT NOT NULL, " +
                            "  `provider_name` TEXT, " +
                            "  `item_name` TEXT, " +
                            "  `quantity_present` INTEGER, " +
                            "  `quantity_ordered` INTEGER, " +
                            "  `portions_per_weekend` INTEGER, " +
                            "  `inventory_closure_date` TEXT, " +
                            "  `delivery_date` TEXT, " +
                            "  `barcode` TEXT, " +
                            "  `note` TEXT, " +
                            "  `creation_date` TEXT, " +
                            "  `last_update` TEXT, " +
                            "  `item_id` TEXT, " +
                            "  PRIMARY KEY(`id`)" +
                            ");"
            );

            // 3) Copio i dati, mettendo NULL in item_id
            db.execSQL(
                    "INSERT INTO `item_history_new` (" +
                            "  `id`, `provider_name`, `item_name`, `quantity_present`, " +
                            "  `quantity_ordered`, `portions_per_weekend`, `inventory_closure_date`, " +
                            "  `delivery_date`, `barcode`, `note`, `creation_date`, `last_update`, `item_id`" +
                            ") SELECT " +
                            "  `id`, `provider_name`, `item_name`, `quantity_present`, " +
                            "  `quantity_ordered`, `portions_per_weekend`, `inventory_closure_date`, " +
                            "  `delivery_date`, `barcode`, `note`, `creation_date`, `last_update`, NULL " +
                            "FROM `ITEM_HISTORY`;"
            );

            // 4) Elimino la vecchia e rinomino
            db.execSQL("DROP TABLE `ITEM_HISTORY`;");
            db.execSQL("ALTER TABLE `item_history_new` RENAME TO `ITEM_HISTORY`;");

            // 5) Ricreo l'indice su id (come si aspettava Room)
            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_ITEM_HISTORY_id` " +
                            "ON `ITEM_HISTORY`(`id`);"
            );

            // 6) Riabilito i vincoli
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    };


}
