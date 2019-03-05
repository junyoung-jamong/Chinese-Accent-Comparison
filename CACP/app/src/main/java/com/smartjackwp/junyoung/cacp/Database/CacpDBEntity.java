package com.smartjackwp.junyoung.cacp.Database;

class CacpDBEntity {
    final static class Contents {
        public static final String TABLE_NAME = "contents";
        public static final String _ID = "id";
        public static final String FILE_PATH = "file_path";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FILE_PATH + " TEXT NOT NULL,"
                    + TITLE + " TEXT NOT NULL,"
                    + DESCRIPTION + " TEXT"
                    + ")";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME  + " ("
                + CacpDBEntity.Contents.FILE_PATH + ", "
                + CacpDBEntity.Contents.TITLE + ", "
                + CacpDBEntity.Contents.DESCRIPTION
                + ") VALUES ";

        public static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;

    }

}
