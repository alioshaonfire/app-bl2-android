package com.buscalibre.app2.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.buscalibre.app2.R;


/**
 * A {@link Fragment} subclass containing useful methods for most fragments.
 */
public class BaseFragment extends Fragment {

    /**
     * Show and hide views. The visibility of the view to hide can be View.GONE or View.INVISIBLE.
     *
     * @param viewToShow The view to show
     * @param viewToHide The view to hide
     * @param gone       True to hide the view using View.GONE
     */
    protected void showHideView(final View viewToShow, final View viewToHide, final boolean gone) {

        // Check if the fragment is added to an activity before. If not, then there is no need
        // to show and hide views. Also, getResources would throw the following exception:
        // java.lang.IllegalStateException: Fragment <Fragment name {id}> not attached to Activity

        if (isAdded()) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            viewToShow.setVisibility(View.VISIBLE);
            viewToShow.animate()
                    .setDuration(shortAnimTime)
                    .alpha(1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            viewToShow.setVisibility(View.VISIBLE);
                        }
                    });

            viewToHide.setVisibility(gone ? View.GONE : View.INVISIBLE);
            viewToHide.animate()
                    .setDuration(shortAnimTime)
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            viewToHide.setVisibility(gone ? View.GONE : View.INVISIBLE);
                        }
                    });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain the fragment's "hidden" state, which is currently lost due an Android bug
        // More info: https://issuetracker.google.com/issues/36992082#comment4
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("isHidden", false)) {
                getFragmentManager().beginTransaction().hide(this).commit();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Retain the fragment's "hidden" state, which is currently lost due an Android bug
        // More info: https://issuetracker.google.com/issues/36992082#comment4
        outState.putBoolean("isHidden", isHidden());
    }

    /**
     * Handles a {@link VolleyError} showing an error dialog to the user
     *
     * @param error      Error to handle
     * @param alertTitle Title of the alert to show to the user
     */
    public void handleVolleyError(VolleyError error, String alertTitle) {
        // Try to obtain the HTTP error code, if present. Otherwise use the name
        // of the error itself as the code to display.
        String errorBody;
        String errorCode;
        if (error instanceof NetworkError) {
            errorBody = getString(R.string.connect_error);
            errorCode = NetworkError.class.getSimpleName();
        } else {
            errorBody = getString(R.string.connect_error);
            errorCode = error.networkResponse != null && error.networkResponse.data != null ?
                    String.valueOf(error.networkResponse.statusCode) : /* AuthFailureError or ServerError */
                    error.getClass().getSimpleName(); /* ParseError or TimeoutError */

        }

        //String errorMessage = errorBody + "\n(" + getString(R.string.error_code) + ": " + errorCode + ")";
        //GraphicUtils.showDialog(getActivity(), alertTitle, errorMessage);
    }
}
