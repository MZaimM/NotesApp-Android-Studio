package com.example.consumerapp.db;

import android.net.Uri;
import android.provider.BaseColumns;


public class DatabaseContract {

    public static final String AUTHORITY = "com.example.mynotesapp";
    public static final String SCHEME = "content";

    public DatabaseContract() {
    }


    public static final class NoteColumns implements BaseColumns{
        public static String TABLE_NAME = "note";

        //note title
        public static String TITLE = "title";

        //note description
        public static String DESCRIPTION = "description";

        //note date
        public static String DATE = "date";

        public static final Uri CONTENT_URI = new Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build();
    }


}
