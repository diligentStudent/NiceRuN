package com.example.NiceRun.main.running;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.NiceRun.R;
import com.google.android.material.tabs.TabLayout;

public class RunMainFragment extends Fragment {
    View root;
    ViewPager viewPager;
    TabLayout tabLayout;
    RunMainPageAdapter adapter;

    public RunMainFragment() {

    }

    public static RunMainFragment getInstance(){
        return  new RunMainFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_running_main, container, false);
        viewPager = root.findViewById(R.id.pager);
        tabLayout = root.findViewById(R.id.tab_layout);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setUpViewPager(ViewPager viewPager){
        adapter = new RunMainPageAdapter(getChildFragmentManager());

        adapter.addFragment(new RunningStartTab(), "바로시작");
        adapter.addFragment(new RecommandTracksTab(), "추천트랙");
        adapter.addFragment(new SavedTracksTab(), "저장트랙");

        viewPager.setAdapter(adapter);
    }


}