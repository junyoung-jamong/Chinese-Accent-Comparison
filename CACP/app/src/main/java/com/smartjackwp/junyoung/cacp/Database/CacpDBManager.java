package com.smartjackwp.junyoung.cacp.Database;

public class CacpDBManager {
    public static CacpDBManager dbManager;

    private CacpDBManager(){}

    public static CacpDBManager getInstance()
    {
        if(dbManager == null)
            dbManager = new CacpDBManager();

        return dbManager;
    }



}
