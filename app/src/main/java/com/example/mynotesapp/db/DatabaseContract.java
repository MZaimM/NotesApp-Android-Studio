package com.example.mynotesapp.db;

import android.provider.BaseColumns;

public class DatabaseContract {

    public static final class NoteColumns implements BaseColumns{
        public static String TABLE_NAME = "note";

        //note title
        public static String TITLE = "title";

        //note description
        public static String DESCRIPTION = "description";

        //note date
        public static String DATE = "date";
    }
}
