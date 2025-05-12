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
import java.nio.charset.StandardCharsets;

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
        version = 415)
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
                    boolean hasPrebuilt = assetExists(context, "database/pioppi.db");
                    RoomDatabase.Builder<AppDatabase> builder = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    APP_DATABASE
                            );

                    if (hasPrebuilt) {
                        builder = builder.createFromAsset("database/pioppi.db");
                    } else {
                        builder = builder
                                .addCallback(new RoomDatabase.Callback() {
                                    @Override
                                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                        super.onCreate(db);
                                        // lancio i miei script SQL
                                        executeSqlFile(db, context, "database/sql/create_tables.sql");
                                        executeSqlFile(db, context, "database/sql/insert_providers.sql");
                                        executeSqlFile(db, context, "database/sql/insert_item.sql");
                                        executeSqlFile(db, context, "database/sql/insert_item_detail.sql");
                                        executeSqlFile(db, context, "database/sql/insert_quantity_type.sql");
                                        executeSqlFile(db, context, "database/sql/insert_item_tag.sql");
                                        executeSqlFile(db, context, "database/sql/insert_item_tag_join.sql");

                                    }
                                });
                    }

                    INSTANCE = builder.build();
                }
            }
        }
        return INSTANCE;
    }

    private static boolean assetExists(Context ctx, String assetPath) {
        try (InputStream is = ctx.getAssets().open(assetPath)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void executeSqlFile(SupportSQLiteDatabase db,
                                       Context ctx,
                                       String assetPath) {
        try (InputStream is = ctx.getAssets().open(assetPath)) {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            String[] stmts = new String(bytes, StandardCharsets.UTF_8)
                    .split(";(\\s)*[\\r\\n]");
            for (String sql : stmts) {
                String s = sql.trim();
                if (!s.isEmpty()) {
                    db.execSQL(s);
                }
            }
        } catch (IOException ioe) {
            Log.e("AppDatabase", "Impossibile leggere " + assetPath, ioe);
        }
    }
}
