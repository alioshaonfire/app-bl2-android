package com.buscalibre.app2.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.buscalibre.app2.R;
import com.buscalibre.app2.adapters.MainPagerAdapter;
import com.buscalibre.app2.constants.Preferences;
import com.buscalibre.app2.util.DepthPageTransformer;
import com.buscalibre.app2.util.FontUtil;
import com.buscalibre.app2.util.ViewPagerCustomDuration;
import com.squareup.picasso.Picasso;
import io.realm.Realm;

public class TutorialDialog  extends DialogFragment {

    ViewPagerCustomDuration vpTutorialSlider;
    LinearLayout layoutTutorialDots;
    Button btSkipTutorial;
    Button btNextTutorial;
    CheckBox cbNotShowTutorial;

    private Realm realm;
    private MainPagerAdapter pagerAdapter;
    private TextView[] dots;
    private int currentPage = 0;
    private final String NEXT_ES = "SIGUIENTE";
    private final String NEXT_EN = "NEXT";
    private final String SKIP_ES = "SALTAR";
    private final String SKIP_EN = "SKIP";
    private final String SHOW_STEPS_ES = "VER PASOS";
    private final String SHOW_STEPS_EN = "SHOW STEPS";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_tutorial, container, false);
        vpTutorialSlider = rootView.findViewById(R.id.vpTutorialSlider);
        layoutTutorialDots = rootView.findViewById(R.id.layoutTutorialDots);

        btSkipTutorial = rootView.findViewById(R.id.btSkipTutorial);
        btNextTutorial = rootView.findViewById(R.id.btNextTutorial);
        cbNotShowTutorial = rootView.findViewById(R.id.cbNotShowTutorial);

        realm = Realm.getDefaultInstance();
        initViewPager();
        //initTyperfaces();
        initViews();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
    }

    private void initViews(){
        btNextTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTutorialOnClick();
            }
        });
        btSkipTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipTutorialOnClick();
            }
        });
        cbNotShowTutorial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setIsTutorialHide(getActivity(), isChecked);
            }
        });
    }

    private void initTyperfaces(){
        btNextTutorial.setTypeface(FontUtil.getMonserratLightTypeface(getActivity()));
        btSkipTutorial.setTypeface(FontUtil.getMonserratLightTypeface(getActivity()));
    }
    private void addBottomDots(int currentPage) {
        Log.e("currentPage",String.valueOf(currentPage));
        dots = new TextView[6];
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);
        layoutTutorialDots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(getActivity());
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            layoutTutorialDots.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void initViewPager(){

        addBottomDots(0);
        pagerAdapter = new MainPagerAdapter();
        vpTutorialSlider.setAdapter(pagerAdapter);
        vpTutorialSlider.setPageTransformer(true, new DepthPageTransformer());

        //Page 1
        LayoutInflater inflater0 = getActivity().getLayoutInflater();
        FrameLayout frameLayout0 = (FrameLayout) inflater0.inflate(R.layout.party_description_viewpager_row, null);
        ImageView ivBannerPartyDescriptionRow0 = frameLayout0.findViewById(R.id.ivBannerPartyDescriptionRow);
        ivBannerPartyDescriptionRow0.setVisibility(View.VISIBLE);
        ivBannerPartyDescriptionRow0.setScaleType(ImageView.ScaleType.FIT_XY);
        ivBannerPartyDescriptionRow0.setImageDrawable(getResources().getDrawable(R.drawable.vender));
        TextView tvTutorialDesc0 = frameLayout0.findViewById(R.id.tvTutorialDesc);
        tvTutorialDesc0.setText(R.string.tut_0_desc);
        TextView tvTutorialTitle0 = frameLayout0.findViewById(R.id.tvTutorialTitle);
        tvTutorialTitle0.setText(R.string.tut_0);
        pagerAdapter.addView(frameLayout0, 0);
        pagerAdapter.notifyDataSetChanged();

        //Page 2
        LayoutInflater inflater1 = getActivity().getLayoutInflater();
        FrameLayout frameLayout = (FrameLayout) inflater1.inflate(R.layout.party_description_viewpager_row, null);
        ImageView ivBannerPartyDescriptionRow = frameLayout.findViewById(R.id.ivBannerPartyDescriptionRow);
        ivBannerPartyDescriptionRow.setScaleType(ImageView.ScaleType.FIT_XY);
        ivBannerPartyDescriptionRow.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_lupa));
        TextView tvTutorialDesc1 = frameLayout.findViewById(R.id.tvTutorialDesc);
        tvTutorialDesc1.setText(R.string.tut_01);
        TextView tvTutorialTitle1 = frameLayout.findViewById(R.id.tvTutorialTitle);
        tvTutorialTitle1.setText(R.string.step1);
        tvTutorialTitle1.setTypeface(Typeface.DEFAULT_BOLD);
        tvTutorialTitle1.setTextColor(getResources().getColor(R.color.oran_bl));
        pagerAdapter.addView(frameLayout, 1);
        pagerAdapter.notifyDataSetChanged();

        //Page 3
        LayoutInflater inflater2 = getActivity().getLayoutInflater();
        FrameLayout frameLayout2 = (FrameLayout) inflater2.inflate(R.layout.party_description_viewpager_row, null);
        ImageView ivBannerPartyDescriptionRow2 = frameLayout2.findViewById(R.id.ivBannerPartyDescriptionRow);
        ivBannerPartyDescriptionRow2.setScaleType(ImageView.ScaleType.FIT_XY);
        ivBannerPartyDescriptionRow2.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_libro));
        TextView tvTutorialDesc2 = frameLayout2.findViewById(R.id.tvTutorialDesc);
        tvTutorialDesc2.setText(R.string.tut_02);
        TextView tvTutorialTitle2 = frameLayout2.findViewById(R.id.tvTutorialTitle);
        tvTutorialTitle2.setText(R.string.step2);
        tvTutorialTitle2.setTypeface(Typeface.DEFAULT_BOLD);
        tvTutorialTitle2.setTextColor(getResources().getColor(R.color.oran_bl));
        pagerAdapter.addView(frameLayout2, 2);
        pagerAdapter.notifyDataSetChanged();

        //Page 4
        LayoutInflater inflater3 = getActivity().getLayoutInflater();
        FrameLayout frameLayout3 = (FrameLayout) inflater3.inflate(R.layout.party_description_viewpager_row, null);
        ImageView ivBannerPartyDescriptionRow3 = frameLayout3.findViewById(R.id.ivBannerPartyDescriptionRow);
        ivBannerPartyDescriptionRow3.setScaleType(ImageView.ScaleType.FIT_XY);
        ivBannerPartyDescriptionRow3.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_carrito));
        TextView tvTutorialDesc3 = frameLayout3.findViewById(R.id.tvTutorialDesc);
        tvTutorialDesc3.setText(R.string.tut_03);
        TextView tvTutorialTitle3 = frameLayout3.findViewById(R.id.tvTutorialTitle);
        tvTutorialTitle3.setText(R.string.step3);
        tvTutorialTitle3.setTypeface(Typeface.DEFAULT_BOLD);
        tvTutorialTitle3.setTextColor(getResources().getColor(R.color.oran_bl));
        pagerAdapter.addView(frameLayout3, 3);
        pagerAdapter.notifyDataSetChanged();

        //Page 5
        LayoutInflater inflater4 = getActivity().getLayoutInflater();
        FrameLayout frameLayout4 = (FrameLayout) inflater4.inflate(R.layout.party_description_viewpager_row, null);
        ImageView ivBannerPartyDescriptionRow4 = frameLayout4.findViewById(R.id.ivBannerPartyDescriptionRow);
        ivBannerPartyDescriptionRow4.setScaleType(ImageView.ScaleType.FIT_XY);
        ivBannerPartyDescriptionRow4.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_pagina));
        TextView tvTutorialDesc4 = frameLayout4.findViewById(R.id.tvTutorialDesc);
        tvTutorialDesc4.setText(R.string.tut_04);
        tvTutorialDesc4.setTextSize(17f);
        TextView tvTutorialTitle4 = frameLayout4.findViewById(R.id.tvTutorialTitle);
        tvTutorialTitle4.setText(R.string.step4);
        tvTutorialTitle4.setTypeface(Typeface.DEFAULT_BOLD);
        tvTutorialTitle4.setTextColor(getResources().getColor(R.color.oran_bl));
        pagerAdapter.addView(frameLayout4, 4);
        pagerAdapter.notifyDataSetChanged();

        //Page 6
        LayoutInflater inflater5 = getActivity().getLayoutInflater();
        FrameLayout frameLayout5 = (FrameLayout) inflater5.inflate(R.layout.party_description_viewpager_row, null);
        ImageView ivBannerPartyDescriptionRow5 = frameLayout5.findViewById(R.id.ivBannerPartyDescriptionRow);
        ivBannerPartyDescriptionRow5.setScaleType(ImageView.ScaleType.FIT_XY);
        ivBannerPartyDescriptionRow5.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_ahorro));
        TextView tvTutorialDesc5 = frameLayout5.findViewById(R.id.tvTutorialDesc);
        tvTutorialDesc5.setText(R.string.tut_05);
        TextView tvTutorialTitle5 = frameLayout5.findViewById(R.id.tvTutorialTitle);
        tvTutorialTitle5.setText(R.string.step5);
        tvTutorialTitle5.setTypeface(Typeface.DEFAULT_BOLD);
        tvTutorialTitle5.setTextColor(getResources().getColor(R.color.oran_bl));
        pagerAdapter.addView(frameLayout5, 5);
        pagerAdapter.notifyDataSetChanged();



        vpTutorialSlider.addOnPageChangeListener(viewPagerPageChangeListener);
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            currentPage = position;
            addBottomDots(position);
            layoutTutorialDots.bringToFront();
            if (position == 5 || position > 5){
                btNextTutorial.setText(R.string.text237);
            }else {
                btNextTutorial.setText(R.string.text238);
            }
            if (position == 0){
                btSkipTutorial.setText(R.string.text239);
                btNextTutorial.setText(R.string.ver_pasos);
            }else {
                btSkipTutorial.setText(R.string.text240);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    //@OnClick(R.id.btSkipTutorial)
    public void skipTutorialOnClick(){
        if (btSkipTutorial.getText().toString().equals(SKIP_ES) || btSkipTutorial.getText().toString().equals(SKIP_EN)){
            getDialog().dismiss();
        }else {
            vpTutorialSlider.setCurrentItem(currentPage - 1);
        }
    }

    //@OnClick(R.id.btNextTutorial)
    public void nextTutorialOnClick(){
        if (btNextTutorial.getText().toString().equals(NEXT_EN) || btNextTutorial.getText().toString().equals(NEXT_ES) || btNextTutorial.getText().toString().equals(SHOW_STEPS_ES) || btNextTutorial.getText().toString().equals(SHOW_STEPS_EN)){
            vpTutorialSlider.setCurrentItem(currentPage + 1);
        }else {
            getDialog().dismiss();
        }
    }
}
