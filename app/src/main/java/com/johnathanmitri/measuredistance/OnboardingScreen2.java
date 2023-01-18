package com.johnathanmitri.measuredistance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.johnathanmitri.measuredistance.databinding.FragmentOnboardingScreen2Binding;

public class OnboardingScreen2 extends Fragment
{
    private FragmentOnboardingScreen2Binding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentOnboardingScreen2Binding.inflate(inflater, container, false);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //set the height based on the width. it should fill out the whole width of the screen, but maintain aspect ratio.
        binding.imageView.getLayoutParams().height = (int)(displayMetrics.widthPixels * (1600.0/1450.0));  //the demonstration png is 1600 x 1450.
        binding.imageView.requestLayout();

        binding.button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // set isFirstRun to false, now that the user has clicked Done.
                SharedPreferences prefs = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirstRun", false);
                editor.apply();

                // transition to the main screen
                FragmentTransaction transaction =  getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,0,0);
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
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
}