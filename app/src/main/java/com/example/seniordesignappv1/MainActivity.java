package com.example.seniordesignappv1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;





//import com.google.android.material.bottomnavigation.BottomNavigationView;

//import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;
import static com.example.seniordesignappv1.BluedoorDBHelper.COLUMN_ID;
import static com.example.seniordesignappv1.BluedoorDBHelper.COLUMN_PASSWORD;
import static com.example.seniordesignappv1.BluedoorDBHelper.COLUMN_USERNAME;
import static java.sql.Types.NULL;

public class MainActivity extends Activity {
    String uniqueID = "jonsmith";
    String UMIDpassword = "12345678";
    int numberEntered;
    EditText UMnameEntered;
    EditText UMpasswordEntered;
    TextView incorrectUMIDdisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        incorrectUMIDdisplay = (TextView)findViewById(R.id.incorrectUMIDdisplay);
        UMnameEntered = (EditText)findViewById(R.id.nameInput);
        UMpasswordEntered = (EditText)findViewById(R.id.passwordInput);


        long rowNumber;

        final BluedoorDBHelper BluedoorUserDB = new BluedoorDBHelper(MainActivity.this);
        //ArrayList listOfUsers = BluedoorUserDB.GetUsers();

        //ArrayList<HashMap<String, String>> userList = BluedoorUserDB.GetUsers();

        //Log.i(TAG, "listOfUsers object: " + userList.toString());




        Log.i(TAG, "Number of Database Entries after deleteUser(1): " + BluedoorUserDB.getNumberofDatabaseEntries());




        /*******************************
         *
         * Insert user code below
         *
         *******************************/

        /*
        rowNumber = BluedoorUserDB.insertUser("jonsmith","12345678");
        Log.i(TAG, "Row ID returned from insertUser: " + rowNumber);


        rowNumber = BluedoorUserDB.insertUser("jjreiman","57747645");
        Log.i(TAG, "Row ID returned from insertUser: " + rowNumber);

         */


        /*******************************
         *
         * Deleting user code below
         *
         *******************************/

        /*
        Log.i(TAG, "Number of Database Entries: " + BluedoorUserDB.getNumberofDatabaseEntries());

        //BluedoorUserDB.deleteUser(5);
        //BluedoorUserDB.deleteUser(4);
        //BluedoorUserDB.deleteUser(3);

        Log.i(TAG, "Number of Database Entries after deleteUser(6): " + BluedoorUserDB.getNumberofDatabaseEntries());
         */





        //button to next page
        Button nextButton = (Button)findViewById(R.id.next_button);
        //event handler for next page
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(checkUMID(BluedoorUserDB)) {
                    Log.i(TAG, "Inside MainActivity.java onClick button call.\n Database name:" + BluedoorUserDB.getDatabaseName());

                    startActivity(new Intent(MainActivity.this, survey.class));
                }
                else
                {
                    incorrectUMIDdisplay.setText("Incorrect name/password entered");
                }
            }
        });


        Button registerButton = (Button)findViewById(R.id.register_button);
        //event handler for next page
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(checkUMID(BluedoorUserDB)) {
                    //Log.i(TAG, "Inside MainActivity.java onClick button call.\n Database name:" + BluedoorUserDB.getDatabaseName());

                    Log.i(TAG, "This UMID is already in the database");
                    return;
                }
                else if(UMnameEntered.getText().toString() == null || UMnameEntered.getText().toString().isEmpty())
                {
                    Log.i(TAG, "Blank name");
                    return;
                }
                else if(UMpasswordEntered.getText().toString() == null || UMpasswordEntered.getText().toString().isEmpty())
                {
                    Log.i(TAG, "Blank password");
                    return;
                }
                else
                {
                    Log.i(TAG, "Entering username");
                    long rowNumber = BluedoorUserDB.insertUser(UMnameEntered.getText().toString(),UMpasswordEntered.getText().toString());
                    Log.i(TAG, "User entry successfully inserted. Database row: " + rowNumber);
                }
            }
        });
    }

    public boolean checkUMID(BluedoorDBHelper db)
    {

        /********************************************************
         *
         * Delete return true; and uncomment the other stuff
         *
         ********************************************************/

        //return true;


        /*******************************************************
         *
         * This code assumes each username is unique.
         * It checks for the username, and once it is found,
         * it checks to make sure the password is equal.
         *
         *******************************************************/

        //pulls all objects from database
        ArrayList<HashMap<String, String>> userList = db.GetUsers();

        //Log.i(TAG, "ID of element number 0: " + db.GetUserByUserId(7));


        //db.printDatabaseEntries();

        //Log.i(TAG, "Printing objects in database: \n" + db.printDatabaseEntries());

        Log.i(TAG, "listOfUsers object: " + userList.toString());

        Log.i(TAG, "listOfUsers size: " + userList.size());

        //Log.i(TAG, "First name inlistOfUsers: " + userList.);


        if(userList.isEmpty())
        {
            Log.i(TAG, "Userlist is empty");
            return false;
        }

        String dummyString = userList.get(1).get(COLUMN_PASSWORD);

        //Log.i(TAG, "dummyString: " + dummyString);


        //if(UMnameEntered.getText().toString().isEmpty())


        for(int i = 0; i < db.getNumberofDatabaseEntries(); i++)
        {


            if(UMnameEntered.getText().toString().equals(userList.get(i).get(COLUMN_USERNAME)))
            {
                if(UMpasswordEntered.getText().toString().equals(userList.get(i).get(COLUMN_PASSWORD)))
                {
                    return true;
                }
                else
                {
                    Log.i(TAG, "Correct username, but wrong password.");
                }
            }
        }



        return false;

    }

}