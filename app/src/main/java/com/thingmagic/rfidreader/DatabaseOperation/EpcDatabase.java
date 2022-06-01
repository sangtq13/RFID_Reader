package com.thingmagic.rfidreader.DatabaseOperation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class EpcDatabase extends SQLiteOpenHelper {

    private static final String TAG = "myDataBaseAssetManager";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "assetManager.db";
    // Table asset manager
    private static final String TABLE_ASSET_MANAGER = "tableAssetManager";
    private static final String COLUMN_ASSET_MANAGER_ID ="Assest_Id";
    private static final String COLUMN_ASSET_MANAGER_LABEL ="Asset_Label";
    private static final String COLUMN_ASSET_MANAGER_EPC = "Asset_EPC";

    public EpcDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "MyDataBaseAssetManager.onCreate ... ");
        // Script create table
        String sql = "CREATE TABLE " + TABLE_ASSET_MANAGER + "("
                + COLUMN_ASSET_MANAGER_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_ASSET_MANAGER_LABEL + " TEXT,"
                + COLUMN_ASSET_MANAGER_EPC + " TEXT" + ")";
        // Execute script.
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "MyDataBaseAssetManager.onUpgrade ... ");

        // Drop old table if it existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSET_MANAGER);
        // Create table again
        onCreate(db);
    }

    public ProductInfo findAssetInfo(String assetLabel) {
        String query = "Select * FROM " + TABLE_ASSET_MANAGER + " WHERE " +
        COLUMN_ASSET_MANAGER_LABEL + " =  \"" + assetLabel + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ProductInfo assetInfo = new ProductInfo();

        if (cursor.moveToFirst()) {

            cursor.moveToFirst();
            assetInfo.setProductID(Integer.parseInt(cursor.getString(0)));
            assetInfo.setProductLabel(cursor.getString(1));
            assetInfo.setProductEPC(cursor.getString(2));

            cursor.close();
        } else {
            assetInfo = null;
        }
        db.close();

        return assetInfo;
    }

    public boolean addAssetInfo(ProductInfo assetInfo) {
        Log.i(TAG, "MyDataBaseAssetManager.addAssetInfo ... " + assetInfo.getProductLabel());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ASSET_MANAGER_LABEL, assetInfo.getProductLabel());
        values.put(COLUMN_ASSET_MANAGER_EPC, assetInfo.getProductEPC());

        // Insert a asset info line to table
        long result = db.insert(TABLE_ASSET_MANAGER, null, values);

        // Close connect database.
        db.close();

        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }


    public ProductInfo getAssetInfo(int id) {
        Log.i(TAG, "MyDataBaseAssetManager.getAssetInfo ... " + id);

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ASSET_MANAGER, new String[] { COLUMN_ASSET_MANAGER_ID,
                        COLUMN_ASSET_MANAGER_LABEL, COLUMN_ASSET_MANAGER_EPC }, COLUMN_ASSET_MANAGER_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ProductInfo assetInfo = new ProductInfo (Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        // Return assetInfo
        return assetInfo;
    }


    public List<ProductInfo> getAllAssetInfo() {
        Log.i(TAG, "MyDataBaseAssetManager.getAllAssetInfo ... " );

        List<ProductInfo> assetList = new ArrayList<ProductInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSET_MANAGER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Add all asset info to list
        if (cursor.moveToFirst()) {
            do {
                ProductInfo assetInfo = new ProductInfo();
                assetInfo.setProductID(Integer.parseInt(cursor.getString(0)));
                assetInfo.setProductLabel(cursor.getString(1));
                assetInfo.setProductEPC(cursor.getString(2));

                // Add to list
                assetList.add(assetInfo);
            } while (cursor.moveToNext());
        }

        // return note list
        return assetList;
    }

    public int getAssetCount() {
        Log.i(TAG, "MyDataBaseAssetManager.getAssetCount ... " );

        String countQuery = "SELECT  * FROM " + TABLE_ASSET_MANAGER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        // return count
        return count;
    }

    public int updateAssetInfo(ProductInfo assetInfo) {
        Log.i(TAG, "MyDataBaseAssetManager.updateAssetInfo ... "  + assetInfo.getProductLabel());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ASSET_MANAGER_EPC, assetInfo.getProductEPC());

        // updating row
        return db.update(TABLE_ASSET_MANAGER, values, COLUMN_ASSET_MANAGER_LABEL + " = ?",
                new String[]{String.valueOf(assetInfo.getProductLabel())});
    }

    public void deleteAssetInfo(ProductInfo assetInfo) {
        Log.i(TAG, "MyDataBaseAssetManager.updateNote ... " + assetInfo.getProductLabel() );

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ASSET_MANAGER, COLUMN_ASSET_MANAGER_ID + " = ?",
                new String[] { String.valueOf(assetInfo.getProductID()) });
        db.close();
    }
}
