package com.smartjackwp.junyoung.cacp.Database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartjackwp.junyoung.cacp.Entity.AccentContents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class CacpDBManager extends SQLiteOpenHelper {
    private static CacpDBManager dbManager;
    private CacpDBDataAccessObject cacpDAO;

    public static final int DB_VERSION = 1 ;
    public static final String DBFILE_CONTACT = "cacp.db" ;

    public CacpDBManager(Context context) {
        super(context, DBFILE_CONTACT, null, DB_VERSION);
        cacpDAO = CacpDBDataAccessObject.getInstance(this.getWritableDatabase());
    }

    public static CacpDBManager getInstance(Context context) {
        if(dbManager == null)
            dbManager = new CacpDBManager(context);

        return dbManager;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CacpDBEntity.Contents.SQL_CREATE_TABLE) ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // onUpgrade(db, oldVersion, newVersion);
    }

    public CacpDBDataAccessObject getDAO(){
        return this.cacpDAO;
    }
}
