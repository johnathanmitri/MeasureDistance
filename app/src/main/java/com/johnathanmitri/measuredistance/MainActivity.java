package com.johnathanmitri.measuredistance;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

//import com.johnathanmitri.measuredistance.databinding.ActivityMainBinding;
import com.johnathanmitri.measuredistance.databinding.OnboardingPageBinding;
import com.johnathanmitri.measuredistance.databinding.ContentMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    private AppBarConfiguration appBarConfiguration;
    private ContentMainBinding mainBinding;
    //private OnboardingPageBinding onboardingBinding;
    //private

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);




        SharedPreferences regPrefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean isFirstRun = regPrefs.getBoolean("isFirstRun", true);  //default value is true if this value does not exist
        if (isFirstRun)
        {
            //startActivity(new Intent(this, OnboardingActivity.class));
            //onboardingBinding = OnboardingPageBinding.inflate(getLayoutInflater());
            //setContentView(getLayoutInflater().inflate(R.layout.onboarding_page,null));
            //setContentView((onboardingBinding.getRoot()));

            onboardingBinding.button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mainBinding = ContentMainBinding.inflate(getLayoutInflater());
                    setContentView(mainBinding.getRoot());
                    SharedPreferences.Editor editor = regPrefs.edit();
                    editor.putBoolean("isFirstRun", false);
                    editor.apply();
                }
            });
        }
        else
        {
            //mainBinding = ContentMainBinding.inflate(getLayoutInflater());
            //setContentView(mainBinding.getRoot());

            //set fragment to MeasureFragment
        }

        mainBinding = ContentMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        //setSupportActionBar(binding.toolbar);

       // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
       // appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
       // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:


                if (action == KeyEvent.ACTION_DOWN)
                {
                    //Fragment fragment = getFragmentManager().findFragmentById(R.id.FirstFragment);
                    //fragment.toggleFreeze();
                    //Intent intent = new Intent("Freeze");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("freeze_preview"));

                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onBackPressed() {

    }

}
