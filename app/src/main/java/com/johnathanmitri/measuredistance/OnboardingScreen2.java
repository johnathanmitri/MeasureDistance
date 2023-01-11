package com.johnathanmitri.measuredistance;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
//import com.johnathanmitri.measuredistance.databinding.FragmentFirstBinding;
import com.johnathanmitri.measuredistance.databinding.FragmentOnboardingScreen1Binding;
import com.johnathanmitri.measuredistance.databinding.FragmentOnboardingScreen2Binding;

import java.text.DecimalFormat;

public class OnboardingScreen2 extends Fragment
{

    private FragmentOnboardingScreen2Binding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentOnboardingScreen2Binding.inflate(inflater, container, false);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        binding.button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences prefs = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirstRun", false);
                editor.apply();


                FragmentTransaction transaction =  getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,0,0);//, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                transaction.setReorderingAllowed(true).replace(R.id.fragment_view, MeasureFragment.class, null).commit();
            }
        });

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onResume()
    {
        super.onResume();

        //levelView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        //levelView.onPause();

    }


}