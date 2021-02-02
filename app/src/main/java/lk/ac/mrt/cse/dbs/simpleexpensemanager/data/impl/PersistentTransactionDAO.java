package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.DatabaseConstants;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.DatabaseHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO implements TransactionDAO {
    private DatabaseHelper databaseHelper;

    public PersistentTransactionDAO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }


    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());

        //Making sure it doesnt get logged if no sufficient funds are available
        String query = "SELECT " + DatabaseConstants.COLUMN_BALANCE + " FROM " + DatabaseConstants.ACCOUNT_TABLE + " WHERE " + DatabaseConstants.COLUMN_ACCOUNT_NO + " =?;";
        Cursor cursor = db.rawQuery(query, new String[]{accountNo});
        if (cursor.moveToFirst()){
            double balance = cursor.getDouble(0);
            cursor.close();
            if (expenseType == ExpenseType.INCOME || balance - amount >= 0){
                ContentValues cv = new ContentValues();
                cv.put(DatabaseConstants.COLUMN_DATE, dateFormat.format(date));
                cv.put(DatabaseConstants.COLUMN_ACCOUNT_NO, accountNo);
                cv.put(DatabaseConstants.COLUMN_EXPENSE_TYPE, String.valueOf(expenseType));
                cv.put(DatabaseConstants.COLUMN_AMOUNT, amount);
                db.insert(DatabaseConstants.TRANSACTION_TABLE, null, cv);
            }
        }
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseConstants.TRANSACTION_TABLE + " ORDER BY " + DatabaseConstants.COLUMN_TRANSACTION_ID + " desc" ;
        Cursor cursor = db.rawQuery(query, null);
        return getTransactionList(cursor);
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseConstants.TRANSACTION_TABLE + " ORDER BY " + DatabaseConstants.COLUMN_TRANSACTION_ID + " desc LIMIT ?";
        Cursor cursor = db.rawQuery(query, new String[]{Integer.toString(limit)});
        return getTransactionList(cursor);
    }


    private List<Transaction> getTransactionList(Cursor cursor){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());

        List<Transaction> transactionList = new ArrayList<>();


        while (cursor.moveToNext()){
            Log.d("DB DATE", cursor.getString(1));
            Date date = new Date();
            try {
                date = dateFormat.parse(cursor.getString(1));
                Log.d("DATE AFTER", String.valueOf(date));

            } catch(ParseException e ) {
                e.printStackTrace();
            }
            String accountNo = cursor.getString(2);
            ExpenseType expenseType = ExpenseType.valueOf(cursor.getString(3));
            double amount = cursor.getDouble(4);
            Transaction transaction = new Transaction(date, accountNo, expenseType, amount);
            transactionList.add(transaction);
        }
        cursor.close();
        return transactionList;
    }
}


