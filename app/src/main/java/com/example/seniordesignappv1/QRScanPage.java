package com.example.seniordesignappv1;
import android.app.Activity;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.blikoon.qrcodescanner.QrCodeActivity;

public class QRScanPage extends Activity {
    private static final int REQUEST_CODE_QR_SCAN = 101;
    private final String LOGTAG = "thirdPageActivity";

    String password = "excursion3";
    String qrScanResult;
    TextView resultDisplay;
    boolean isVerified;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan_page);
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

        isVerified = false;
        //button to next page
        Button scanButton = (Button)findViewById(R.id.qrScanButton);
        //button to last page
        Button backButton3 = (Button)findViewById(R.id.backButton3);
        //textview for verification message
        resultDisplay = (TextView)findViewById(R.id.scanResult);
        //event handler for scan button
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(QRScanPage.this,QrCodeActivity.class);
                startActivityForResult( i,REQUEST_CODE_QR_SCAN);
            }
        });

        /****************************
         *
         * Should this be finish()?
         *
         ****************************/
        //event handler for back page button
        backButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(QRScanPage.this, survey.class));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK)
        {
            Log.d(LOGTAG,"COULD NOT GET A GOOD RESULT.");
            if(data==null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if( result!=null)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(QRScanPage.this).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            return;

        }
        if(requestCode == REQUEST_CODE_QR_SCAN)
        {
            if(data==null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            Log.d(LOGTAG,"Have scan result in your app activity :"+ result);
            /*
            AlertDialog alertDialog = new AlertDialog.Builder(thirdPageActivity.this).create();
            alertDialog.setTitle("Scan result");
            alertDialog.setMessage(result);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


            alertDialog.show();

             */
            //qrScanResult = result;
            if(result.equals(password)) {
                resultDisplay.setText("Correct Code! You are verified!");
                resultDisplay.setTextColor(getResources().getColor(R.color.green));
                isVerified = true;

                final Intent intent = new Intent(this, BLEScanPage.class);
                intent.putExtra("EXTRAS_DEVICE_SCAN_TOGGLE", true);
                intent.putExtra("EXTRAS_DEVICE_DUMMY", "Secret Message");
                startActivity(intent);

                /***************************************************
                 *
                 * Write time remaining(8 hours) and verified bool
                 * to database here.
                 *
                 ***************************************************/
            }
            else
            {
                resultDisplay.setText("Incorrect QR code. Please proceed to Campus and take a non-contact temperature test.");
                resultDisplay.setTextColor(getResources().getColor(R.color.red));
                isVerified = false;
            }

        }
    }
}