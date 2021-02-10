package com.example.seniordesignappv1;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.text.style.BulletSpan;
import android.text.SpannableString;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.view.View;


//import com.google.android.material.bottomnavigation.BottomNavigationView;

//import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class survey extends Activity {
    //first question button setup
    //private RadioGroup radioGroup1;
    //private RadioButton radioButton1;
    //private Button btnDisplay1;

    //the boolean variables for user response
    boolean q1boolean;
    boolean q2boolean;
    boolean q3boolean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        //BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        /*
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();






         */
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //NavigationUI.setupWithNavController(navView, navController);

        //set the boolean all to true, then user has to make them false
        q1boolean = true;
        q2boolean = true;
        q3boolean = true;

        final TextView resultsDisplay = (TextView)findViewById(R.id.surveyResultsDisplay);


        //button to next page
        Button nextButton2 = (Button)findViewById(R.id.nextButton2);
        //button to last page
        Button backButton2 = (Button)findViewById(R.id.backButton2);
        //event handler for next page
        nextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAnswers() == false) {
                    startActivity(new Intent(survey.this, QRScanPage.class));
                }
                else
                {
                    Log.i("SurveyActivity", "you put the wrong answers in");
                    resultsDisplay.setText(R.string.survey_incorrect_text);

                }
            }
        });

        //event handler for back page
        backButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(survey.this, MainActivity.class));
            }
        });

        //the textviews for the lists
        TextView q1list = (TextView) findViewById(R.id.q1list);
        TextView q2list = (TextView) findViewById(R.id.q2list);
        TextView q3list = (TextView) findViewById(R.id.q3list);

        //setting up the bulleted list for the sruvey question 1
        CharSequence t1 = getText(R.string.bullet_list_item1);
        SpannableString s1 = new SpannableString(t1);
        s1.setSpan(new BulletSpan(15), 0, t1.length(), 0);
        CharSequence t2 = getText(R.string.bullet_list_item2);
        SpannableString s2 = new SpannableString(t2);
        s2.setSpan(new BulletSpan(15), 0, t2.length(), 0);
        CharSequence t3 = getText(R.string.bullet_list_item3);
        SpannableString s3 = new SpannableString(t3);
        s3.setSpan(new BulletSpan(15), 0, t3.length(), 0);
        q1list.setText(TextUtils.concat(s1, s2, s3));

        //setting up the bulleted list for the sruvey question 2
        t1 = getText(R.string.bullet_list_item4);
        s1 = new SpannableString(t1);
        s1.setSpan(new BulletSpan(15), 0, t1.length(), 0);
        t2 = getText(R.string.bullet_list_item5);
        s2 = new SpannableString(t2);
        s2.setSpan(new BulletSpan(15), 0, t2.length(), 0);
        t3 = getText(R.string.bullet_list_item6);
        s3 = new SpannableString(t3);
        s3.setSpan(new BulletSpan(15), 0, t3.length(), 0);
        q2list.setText(TextUtils.concat(s1, s2, s3));

        //setting up the bulleted list for the sruvey question 3
        t1 = getText(R.string.bullet_list_item7);
        s1 = new SpannableString(t1);
        s1.setSpan(new BulletSpan(15), 0, t1.length(), 0);
        t2 = getText(R.string.bullet_list_item8);
        s2 = new SpannableString(t2);
        s2.setSpan(new BulletSpan(15), 0, t2.length(), 0);
        t3 = getText(R.string.bullet_list_item9);
        s3 = new SpannableString(t3);
        s3.setSpan(new BulletSpan(15), 0, t3.length(), 0);
        q3list.setText(TextUtils.concat(s1, s2, s3));
    }

    //function for radio buttons for q1
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();



        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.q1y:
                if (checked) {
                    Log.i("SurveyActivity", "Question 1 Yes selected");
                    q1boolean = true;

                    break;
                }
            case R.id.q1n:
                if (checked) {
                    Log.i("SurveyActivity", "Question 1 No selected");
                    q1boolean = false;
                    break;
                }
        }

        //question 2
        switch(view.getId()) {
            case R.id.q2y:
                if (checked) {
                    Log.i("SurveyActivity", "Question 2 Yes selected");
                    q2boolean = true;
                    break;
                }
            case R.id.q2n:
                if (checked) {
                    Log.i("SurveyActivity", "Question 2 No selected");
                    q2boolean = false;
                    break;
                }
        }

        //question 3
        switch(view.getId()) {
            case R.id.q3y:
                if (checked) {
                    Log.i("SurveyActivity", "Question 3 Yes selected");
                    q3boolean = true;
                    break;
                }
            case R.id.q3n:
                if (checked) {
                    Log.i("SurveyActivity", "Question 3 No selected");
                    q3boolean = false;
                    break;
                }
        }

    }

    public boolean checkAnswers()
    {
        if(q1boolean | q2boolean | q3boolean)
        {
            Log.i("SurveyActivity", "not verified");
        }
        else
        {
            Log.i("SurveyActivity", "VERIFIED CORRECTLY");
        }
        return(q1boolean | q2boolean | q3boolean);
    }



}