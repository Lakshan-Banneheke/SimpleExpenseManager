package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.DatabaseConstants;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.DatabaseHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {
    private DatabaseHelper databaseHelper;

    public PersistentAccountDAO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public List<String> getAccountNumbersList() {
        List<String> accountNumbers = new ArrayList<>();

        String query = "SELECT " + DatabaseConstants.COLUMN_ACCOUNT_NO + " FROM " + DatabaseConstants.ACCOUNT_TABLE;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()){
             accountNumbers.add(cursor.getString(0));
        }
        cursor.close();
        return accountNumbers;    }

    @Override
    public List<Account> getAccountsList() {
        List<Account> accountList = new ArrayList<>();

        String query = "SELECT * FROM " + DatabaseConstants.ACCOUNT_TABLE;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()){
              String accountNo = cursor.getString(0);
              String bankName = cursor.getString(1);
              String accountHolderName = cursor.getString(2);
              double balance = cursor.getDouble(3);

              Account account = new Account(accountNo, bankName, accountHolderName, balance);
              accountList.add(account);
        }
        cursor.close();
        return accountList;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseConstants.ACCOUNT_TABLE + " WHERE " + DatabaseConstants.COLUMN_ACCOUNT_NO + " =?;";
        Cursor cursor = db.rawQuery(query, new String[]{accountNo});
        if (cursor.moveToFirst()){
            Account account = new Account(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3));
            cursor.close();
            return account;
        } else {
            cursor.close();
            throw new InvalidAccountException("Account Number is invalid");
        }

    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseConstants.COLUMN_ACCOUNT_NO, account.getAccountNo());
        cv.put(DatabaseConstants.COLUMN_BANK_NAME, account.getBankName());
        cv.put(DatabaseConstants.COLUMN_ACCOUNT_HOLDER_NAME, account.getAccountHolderName());
        cv.put(DatabaseConstants.COLUMN_BALANCE, account.getBalance());
        db.insert(DatabaseConstants.ACCOUNT_TABLE, null, cv);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long delete = db.delete(DatabaseConstants.ACCOUNT_TABLE,DatabaseConstants.COLUMN_ACCOUNT_NO + " = ?", new String[]{accountNo});
        if (delete == -1) {
            throw new InvalidAccountException("Account Number is invalid");
        }
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Account account = this.getAccount(accountNo);
        Log.d("Initial Balance", String.valueOf(account.getBalance()));
        double updatedBalance;
        ContentValues cv = new ContentValues();
        switch (expenseType){
            case INCOME:
                updatedBalance = account.getBalance() + amount;
                cv.put(DatabaseConstants.COLUMN_BALANCE, updatedBalance);
                long updateIncome = db.update(DatabaseConstants.ACCOUNT_TABLE, cv,DatabaseConstants.COLUMN_ACCOUNT_NO + " = ?", new String[]{accountNo});
                Log.d("MESSAGE", String.valueOf(updateIncome));
                if (updateIncome == 0) {
                    throw new InvalidAccountException("Database Error");
                }
                break;
            case EXPENSE:
                if (account.getBalance() - amount < 0){
                    throw new InvalidAccountException("Balance insufficient. Available balace is " + account.getBalance());
                } else {
                    updatedBalance = account.getBalance() - amount;
                    cv.put(DatabaseConstants.COLUMN_BALANCE, updatedBalance);
                    long updateExpense = db.update(DatabaseConstants.ACCOUNT_TABLE, cv,DatabaseConstants.COLUMN_ACCOUNT_NO + " = ?", new String[]{accountNo});
                    if (updateExpense == 0) {
                        throw new InvalidAccountException("Database Error");
                    }
                }
                break;


        }
        Log.d("Final Balance", String.valueOf(this.getAccount(accountNo).getBalance()));
    }
}
