package bluetooth.android.bluetoothapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Manju on 12/22/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{

    private static String DBNAME = "Devices";
    private static int VERSION = 1;

    private SQLiteDatabase mDB;

    public DatabaseHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        mDB = getWritableDatabase();

    }

    public SQLiteDatabase getDatabaseInstance()
    {
        return mDB;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql =     "create table "+ DatabaseContentPovider.DATABASE_TABLE + " ( "
                + DatabaseContentPovider.ROW_ID + " integer , "
                + DatabaseContentPovider.DEVICE_NAME + " text  , "
                + DatabaseContentPovider.DEVICE_TIME + " text  , "
                + DatabaseContentPovider.DEVICE_ADDRESS + "  text  , "
                + DatabaseContentPovider.DEVICE_RSSI + "  integer  ) " ;

        db.execSQL(sql);
    }

    public Cursor getAllDevices(){
        return mDB.query(DatabaseContentPovider.DATABASE_TABLE, new String[] { DatabaseContentPovider.ROW_ID,  DatabaseContentPovider.DEVICE_NAME ,
                        DatabaseContentPovider.DEVICE_TIME, DatabaseContentPovider.DEVICE_ADDRESS,
                        DatabaseContentPovider.DEVICE_RSSI } ,
                null, null, null, null,
                DatabaseContentPovider.DEVICE_RSSI + " desc ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
