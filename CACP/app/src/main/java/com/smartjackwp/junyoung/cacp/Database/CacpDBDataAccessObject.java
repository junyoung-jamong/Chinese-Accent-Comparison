package com.smartjackwp.junyoung.cacp.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.smartjackwp.junyoung.cacp.Entity.AccentContents;

import java.util.ArrayList;

public class CacpDBDataAccessObject {
    public static CacpDBDataAccessObject cacpDAO;

    SQLiteDatabase sqliteDB;

    private CacpDBDataAccessObject(SQLiteDatabase sqliteDB)
    {
        this.sqliteDB = sqliteDB;
    }

    static CacpDBDataAccessObject getInstance(SQLiteDatabase sqliteDB)
    {
        if(cacpDAO == null)
            cacpDAO = new CacpDBDataAccessObject(sqliteDB);
        return cacpDAO;
    }

    public int insertContents(AccentContents accentContents)
    {
        try{

            ContentValues values = new ContentValues();
            values.put(CacpDBEntity.Contents.FILE_PATH, accentContents.getFilePath());
            values.put(CacpDBEntity.Contents.TITLE, accentContents.getTitle());
            values.put(CacpDBEntity.Contents.DESCRIPTION, accentContents.getDescription());
            long id = sqliteDB.insert(CacpDBEntity.Contents.TABLE_NAME, null, values);

            return (int)id;

            /*
            String sqlInsert = CacpDBEntity.Contents.SQL_INSERT + "("
                                + "'" + accentContents.getFilePath() + "', "
                                + "'" + accentContents.getTitle() + "', "
                                + "'" + accentContents.getDescription() + "'"
                                + ")";
            sqliteDB.execSQL(sqlInsert) ;
            sqliteDB.insert();

            String selectQuery = "SELECT * FROM sqlite_sequence";
            Cursor cursor = sqliteDB.rawQuery(selectQuery, null);
            cursor.moveToLast();
            int id = cursor.getInt(0) ;
            return id;
            */
        }catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean deleteContents(AccentContents accentContents)
    {
        try{
            String sqlInsert = CacpDBEntity.Contents.SQL_DELETE + " WHERE " + CacpDBEntity.Contents._ID + " = " + accentContents.getId();
            sqliteDB.execSQL(sqlInsert) ;
            return true;
        }catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<AccentContents> selectContents()
    {
        ArrayList<AccentContents> contentsList = new ArrayList<>();
        try{
            String sqlSelect = CacpDBEntity.Contents.SQL_SELECT_ALL;
            Cursor cursor = sqliteDB.rawQuery(sqlSelect, null) ;

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(CacpDBEntity.Contents._ID));
                String filePath = cursor.getString(cursor.getColumnIndex(CacpDBEntity.Contents.FILE_PATH));
                String title = cursor.getString(cursor.getColumnIndex(CacpDBEntity.Contents.TITLE));
                String description = cursor.getString(cursor.getColumnIndex(CacpDBEntity.Contents.DESCRIPTION));

                contentsList.add(new AccentContents(id, filePath, title, description));
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return contentsList;
    }

}
