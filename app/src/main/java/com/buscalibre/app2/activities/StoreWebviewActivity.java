package com.buscalibre.app2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.buscalibre.app2.R;
import com.buscalibre.app2.adapters.StoreListAdapter;
import com.buscalibre.app2.fragments.BrowseFragment;
import com.buscalibre.app2.util.AlertDialogHelper;
import com.buscalibre.app2.util.StoreManager;

public class StoreWebviewActivity extends BaseActivity implements AlertDialogHelper.AlertDialogListener, BrowseFragment.OnBrowseFragmentInteractionListener {

    private OnStoreChosenListener mListener;
    private String storeSelected = "";
    BrowseFragment mBrowseFragment;
    Fragment mCurrentFragment;
    private AlertDialogHelper alertDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_webview);
        initViews();
        initData();
    }

    private void initViews(){
        alertDialogHelper = new AlertDialogHelper(this);
        hideToolbar();
    }

    private void initData(){
        if (getIntent().getExtras() != null){
            storeSelected = getIntent().getStringExtra("storeSelected");

            FragmentTransaction replaceBrowseFragmentTransaction = getSupportFragmentManager().beginTransaction();
            // Remove any previously existing BrowseFragment
            if (mBrowseFragment != null) {
                replaceBrowseFragmentTransaction.remove(mBrowseFragment);
            }
            // Instantiate a new BrowseFragment and add it on top of the main container
            if (storeSelected.equals("amazon")){
                mBrowseFragment = BrowseFragment.newInstance(StoreManager.Store.AMAZON);

            }else if (storeSelected.equals("ebay")){
                mBrowseFragment = BrowseFragment.newInstance(StoreManager.Store.EBAY);

            }else  if (storeSelected.equals("bhphoto")){
                mBrowseFragment = BrowseFragment.newInstance(StoreManager.Store.BHPHOTO);

            }
            replaceBrowseFragmentTransaction
                    .add(R.id.fl_main_frame, mBrowseFragment, mBrowseFragment.TAG)
                    .commit();
            mCurrentFragment = mBrowseFragment;
        }
    }

    @Override
    public void onPositiveClick(int from) {
        if (from == 1){
            finish();
        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }

    @Override
    public void onChooseStoreSelected() {
        showChooseStoreFragment();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void showChooseStoreFragment() {
        //showHideFragment(mChooseStoreFragment, mBrowseFragment);
    }

    @Override
    public void onBackPressed() {
        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text36), getString(R.string.accept_dialog),getString(R.string.cancel_dialog),"",1, false);
    }

    public interface OnStoreChosenListener {
        void onStoreChosen(@StoreManager.Store final String chosenStore);
        void onListChosen(@StoreManager.Store final String chosenStore);
        void onFragmentChosen(@StoreManager.Store final String chosenStore);
    }
    private void showHideFragment(Fragment fragmentToShow, Fragment fragmentToHide) {
        FragmentTransaction showHideFragmentTransaction = getSupportFragmentManager().beginTransaction();
        showHideFragmentTransaction
                .show(fragmentToShow)
                .hide(fragmentToHide)
                .commit();
        mCurrentFragment = fragmentToShow;
    }
}