package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "180070L";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAccountQuery = "CREATE TABLE " + DatabaseConstants.ACCOUNT_TABLE + " (" + DatabaseConstants.COLUMN_ACCOUNT_NO + " TEXT PRIMARY KEY, " + DatabaseConstants.COLUMN_BANK_NAME + " TEXT NOT NULL, " +  DatabaseConstants.COLUMN_ACCOUNT_HOLDER_NAME + " TEXT NOT NULL, " + DatabaseConstants.COLUMN_BALANCE + " REAL NOT NULL CHECK(" + DatabaseConstants.COLUMN_BALANCE + ">= 0))";
        db.execSQL(createAccountQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TRANSACTION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.ACCOUNT_TABLE);
        onCreate(db);
    }
}
