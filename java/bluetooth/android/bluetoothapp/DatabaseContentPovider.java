package bluetooth.android.bluetoothapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Manju on 12/22/2015.
 */
public class DatabaseContentPovider extends ContentProvider
{

    public static final String PROVIDER_NAME = "bluetooth.android.bluetoothapp.provider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/" + DatabaseContentPovider.DATABASE_TABLE);
    DatabaseHelper mDB;

    public static final String ROW_ID = "_id";
    public static final String DEVICE_NAME = "device_name";
    public static final String DATABASE_TABLE = "btDevices";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DEVICE_RSSI = "device_rssi";
    public static final String DEVICE_TIME = "device_time";

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, DatabaseContentPovider.DATABASE_TABLE, 1);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor cursor = null;
        if(uriMatcher.match(uri)== 1){
            cursor = mDB.getAllDevices();
        }
        if (null != cursor)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public boolean onCreate()
    {
        mDB = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        Uri row_uri = null;
        long rowId = mDB.getDatabaseInstance().insert(DatabaseContentPovider.DATABASE_TABLE, "", values);

        if (rowId > 0) {
            row_uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return row_uri;
        }

        return row_uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)

    {
        int count = mDB.getDatabaseInstance().delete(DatabaseContentPovider.DATABASE_TABLE, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Nullable
    @Override
    public String getType(Uri uri)
    {
        return null;
    }
}
