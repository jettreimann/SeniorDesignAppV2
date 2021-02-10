package com.example.seniordesignappv1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.proto.ProtoOutputStream;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class BluedoorDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_USER = "user";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "name";
    public static final String COLUMN_PASSWORD = "password";
    public static final boolean COLUMN_ISVERIFED = false;
    public static final String COLUMN_TIMEVERIFIED = "time";

    //public static final String TIME_VERIFIED = "YYYY-MM-DD hh:mm:ss";

    private static final String DATABASE_NAME = "bluedoor.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_USER + "(" + COLUMN_ID  + " integer primary key autoincrement, "
            + COLUMN_USERNAME + " text not null, "
            + COLUMN_PASSWORD + " text not null);";

    public BluedoorDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //Log.i(TAG, "You made it to the BluedoorDBHelper onCreate()");
        String CREATE_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT"+ ")";



        //execSQL function sets up the database using the string above
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, "Value of CREATE_TABLE after onCreate:" + CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(BluedoorDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    //function for inserting user
    long insertUser(String username, String password)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        //cValues.put(COLUMN_ID, id);
        cValues.put(COLUMN_USERNAME, username);
        cValues.put(COLUMN_PASSWORD, password);




        long newRowId = db.insert(TABLE_USER,null, cValues);
        db.close();
        return  newRowId;

    }

    public void deleteUser(int userid){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, COLUMN_ID+" = ?",new String[]{String.valueOf(userid)});
        db.close();
    }

    public long getNumberofDatabaseEntries()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_USER);

        db.close();
        return count;
    }


    //GetUsers() returns arraylist object type
    public ArrayList<HashMap<String, String>> GetUsers()
    {
        //open the db in writable form
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        String query = "SELECT name, password, _id FROM "+ TABLE_USER;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            HashMap<String,String> user = new HashMap<>();
            user.put(COLUMN_USERNAME,cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)));

            user.put(COLUMN_PASSWORD,cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
            userList.add(user);
        }
        return  userList;
    }


    public String printDatabaseEntries()
    {
        String entryString = "String entryString: \n";
        String[] userInfo = new String[2];
        String strDummy;
        long n = this.getNumberofDatabaseEntries();
        ArrayList<HashMap<String, String>> userList = this.GetUsers();

        //entryString = toString().userList.get(0);

        //Log.i(TAG, "\nPrinting userList.get(0): " + userList.get(0).toString());
        //Log.i(TAG, "\nPrinting userList.get(1): " + userList.get(1).toString());


        for (int i = 0; i < n; i++)
        {
            entryString += "\nUser #" + i + ": ";
            strDummy = userList.get(i).toString();
            strDummy.replace("{", "adfasf");
            userInfo = strDummy.split(",");
            //userInfo[0].replace("{", "adfasf");
            //userInfo[1].replace("}", "adfasf");
            entryString += userInfo[0]+ userInfo[1];

            //entryString += "\nUser #" + i + ": " + userList.get(i).toString().replace("{", "");
            //entryString.replace("\{", "adfasf");

            //Log.i(TAG, "\nDelete first curly: " + entryString);
            //userInfo = userList.get(i).toString().split(",");
        }



        //Log.i(TAG, "\nPrinting userList.get(1): " + userList.get(2).toString());

        entryString.replace("{", "");

        return entryString;
    }








    //get object by id number
    // Get User Details based on userid
    public ArrayList<HashMap<String, String>> GetUserByUserId(int userid){
        //make the database readable
        SQLiteDatabase db = this.getWritableDatabase();

        //make an arrayList object of HashMap objects called userList
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();

        //query function to find the
        String query = "SELECT name, password FROM "+ TABLE_USER;

        //cursor object to sort of "parse" through the objects
        Cursor cursor = db.query(TABLE_USER, new String[]{COLUMN_USERNAME, COLUMN_PASSWORD}, COLUMN_ID+ "=?",new String[]{String.valueOf(userid)},null, null, null, null);

        if (cursor.moveToNext()){
            HashMap<String,String> user = new HashMap<>();
            user.put("name",cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)));

            user.put("password",cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
            userList.add(user);
        }
        return  userList;
    }
}