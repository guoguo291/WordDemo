package com.guoj.worddemo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * ROOM使用
 * 1.创建实体类Entity
 * 2.创建DAO
 * 3.创建databse
 */
@Database(entities = {Word.class}, version = 4, exportSchema = false)
public abstract class WordDatabase extends RoomDatabase {
    private volatile static WordDatabase INSTANCE;

    static WordDatabase getDataBaseInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WordDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), WordDatabase.class, "word_database")
//                            .fallbackToDestructiveMigration()//强制升级破坏原有数据
//                            .addMigrations(MIGRATION2_3)
                            .addMigrations(MIGRATION3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract WordDao getWordDao();
    static final Migration MIGRATION2_3=new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE word ADD COLUMN showMean2 INTEGER NOT NULL DEFAULT 1");
        }
    };
    //删除字段
    static final Migration MIGRATION3_4=new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //1.创建临时表
            database.execSQL("CREATE TABLE word_temp (id INTEGER PRIMARY KEY NOT NULL,english_word TEXT," +
                    "chinese_meaning TEXT,showMean INTEGER NOT NULL DEFAULT 1)");
            //2.将数据插入临时表
            database.execSQL("INSERT INTO word_temp (id,english_word,chinese_meaning,showMean)"+
                    "SELECT id,english_word,chinese_meaning,showMean FROM word");
            //3.删除原来的表
            database.execSQL("DROP TABLE word");
            //4.将临时表改名回原来的表名
            database.execSQL("ALTER TABLE word_temp RENAME to word");
        }
    };

}
