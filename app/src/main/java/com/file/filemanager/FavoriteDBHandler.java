package com.file.filemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by huang on 2018/5/15.
 */

public class FavoriteDBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "favorites.db";
    private static final String FAVORITE_TABLE = "favorite";

    public FavoriteDBHandler(Context context) {
        super(context, DB_NAME, null, 0x01);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE favorite(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "path TEXT NOT NULL, " +
                "status INTEGER, " +
                "time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor queryFavoritePathCursor(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.query(FAVORITE_TABLE, new String[]{"path"}, null, null, null, null, null);
    }

    public void addToFavorite(String path){
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("status", 1);
        values.put("time", System.currentTimeMillis());

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.insert(FAVORITE_TABLE, null, values);
    }

    public void deleteFromFavorite(String path){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete(FAVORITE_TABLE, "path=?", new String[]{path});
    }

    public boolean isPathInFavorite(String path){
        boolean isIn = false;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = sqLiteDatabase.query(FAVORITE_TABLE, new String[]{"path"}, null, null, null, null, null);
        if(null == cursor || 0 == cursor.getCount())
            return false;

        cursor.moveToFirst();
        do{
            String curPath = cursor.getString(cursor.getColumnIndex("path"));
            if(path.equals(curPath)){
                isIn = true;
                break;
            }
        }while(cursor.moveToNext());
        cursor.close();

        return isIn;
    }
}
