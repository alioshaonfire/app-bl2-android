package com.buscalibre.app2.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.buscalibre.app2.R;
import com.buscalibre.app2.activities.BaseWebViewActivity;
import com.buscalibre.app2.constants.ServerConstants;
import com.buscalibre.app2.events.SelectStoreEvent;
import com.buscalibre.app2.models.CartResult;
import com.buscalibre.app2.models.Country;
import com.buscalibre.app2.models.Quotation;
import com.buscalibre.app2.models.QuoteResult;
import com.buscalibre.app2.models.ShippingMethod;
import com.buscalibre.app2.models.UserCart;
import com.buscalibre.app2.models.UserLogin;
import com.buscalibre.app2.network.NetworkToken;
import com.buscalibre.app2.network.NetworkUtil;
import com.buscalibre.app2.network.RestClient;
import com.buscalibre.app2.network.RetrofitClientInstance;
import com.buscalibre.app2.util.AlertDialogHelper;
import com.buscalibre.app2.util.ConfigUtil;
import com.buscalibre.app2.util.GraphicUtils;
import com.buscalibre.app2.util.ProductPageParser;
import com.buscalibre.app2.util.StoreManager;
import com.buscalibre.app2.widgets.PriceView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnBrowseFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BrowseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BrowseFragment extends BaseFragment implements AlertDialogHelper.AlertDialogListener {

    public static final String TAG = BrowseFragment.class.getSimpleName();
    private static final String ARG_STORE = "store";

    // Rows and column numbers needed for updating the price grid
    private static final int ROW_CONDITION_NEW = 1;
    private static final int ROW_CONDITION_NEW_PRIME = 2;
    private static final int ROW_CONDITION_USED = 3;
    private static final int ROW_CONDITION_REFURBISHED = 4;
    private static final int COLUMN_SHIPPING_METHOD_AIRPLANE = 1;
    private static final int COLUMN_SHIPPING_METHOD_SHIP = 2;

    private AlertDialogHelper alertDialogHelper;
    private String storeName = "";
    private String bhURL = "";
    private String mUrl;
    private PriceView mSelectedPrice;
    private String mAnalyticsKeyAmazon = "&utm_source=app_bl&utm_medium=app_bl&utm_campaign=app_te_traemos_amazon";
    private String mAnalyticsKeyEbay = "&utm_source=app_bl&utm_medium=app_bl&utm_campaign=app_te_traemos_ebay";
    private String mAnalyticsKeyCart = "?utm_source=app_bl&utm_medium=app_bl&utm_campaign=app_cart";
    private String mBLCart = "https://www.buscalibre.cl/v2/carro";
    private boolean hasNewDiferenciator = false;
    private String diferentiatorValue = "";
    private Map<String,String> differencitorMap;
    private String urlImage = "";
    private ImageView ivProductImage;


    /**
     * Current {@link State} of this fragment
     */
    @State
    private volatile int mCurrentBrowseState;

    /**
     * Indicates if the state is currently being checked
     */
    private volatile boolean mCheckingState;

    /**
     * Listener for UI interactions
     */
    private OnBrowseFragmentInteractionListener mListener;

    /**
     * Behaviour for the bottom sheet for quoting an item
     */
    private BottomSheetBehavior mQuoteBottomSheetBehavior;

    /**
     * Unique string representing the product currently being displayed.
     * Empty String if no product is currently being displayed.
     */
    private String mCurrentProductHash = "";

    /**
     * Warning to show with some products
     */
    private String mWarning;

    // Timer and task for checking and assigning the current state of the fragment
    private Timer mStatusCheckTimer;
    private TimerTask mStatusCheckTimerTask;

    /**
     * Parser for obtaining the data of the page currently being shown
     */
    ProductPageParser mProductPageParser;
    // UI elements
    private View mRootView;
    private WebView mWebsiteContent;
    private ProgressBar mWebsiteProgressBar;
    private TextView mChooseStoreButton;
    private ImageButton mBrowseBackButton;
    private ImageButton mBrowseForwardButton;
    private ImageButton mQuestionModalButton;
    private ImageButton mCartButton;
    private View mQuoteBottomSheet;
    private View mBackgroundOverlay;
    private View mBrowseToolbarOverlay;
    private View mQuoteBottomSheetHeader;
    private ProgressBar mQuoteBottomSheetHeaderProgressBar;
    private TextView mQuoteBottomSheetHeaderText;
    private ImageView mQuoteBottomSheetHeaderArrow;
    private GridLayout mQuoteBottomSheetPricesGrid;
    private Button mBuyButton;
    private ProgressBar mBuyProgressBar;
    private ImageView ibCloseView;
    private RelativeLayout llCartTool;
    private TextView tvCartQty;

    /**
     * Using layout_marginTop on the bottom sheet causes unwanted behaviour when collapsing it.
     * As a workaround, we add a view that acts as a top margin. Then, its height is programmatically
     * altered between 0 and its initial height when sliding the bottom sheet.
     */
    private View mQuoteBottomSheetMarginTop;

    /**
     * Max top height of {@link #mQuoteBottomSheetMarginTop}, in dps
     */
    private static int BOTTOM_SHEET_MARGIN_TOP_MAX_HEIGHT_DP = 40;

    /**
     * Max top height of {@link #mQuoteBottomSheetMarginTop}, in pixels
     */
    private int mQuoteBottomSheetMarginTopMaxHeight;

    public BrowseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param store {@link StoreManager.Store} string representing the store for this fragment
     * @return A new instance of fragment BrowseFragment.
     */
    public static BrowseFragment newInstance(@StoreManager.Store String store) {
        BrowseFragment fragment = new BrowseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STORE, store);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        showCart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            // Set up the home url and the product page parser according to the current store
            @StoreManager.Store String store = getArguments().getString(ARG_STORE);
            mUrl = StoreManager.getStoreUrl(getActivity(), store);
            mProductPageParser = StoreManager.getProductPageParser(store);
            storeName = store;
        }
    }

    @Override
    public void onDestroy() {
        stopStatusCheck();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mWebsiteContent == null) {
            mRootView = inflater.inflate(R.layout.fragment_browse, container, false);
            mapUiElements(mRootView);
            configureQuoteBottomSheet();
            configureWebView();
            setUiListeners();
            mWebsiteContent.loadUrl(mUrl);
            //showCart();
        }
        return mRootView;
    }

    private void showCart(){
        Country country = Realm.getDefaultInstance().where(Country.class).equalTo("isSelected", true).findFirst();

        getUserCart(country.getId());
        llCartTool.setOnClickListener(v -> {
            UserLogin userLogin = Realm.getDefaultInstance().where(UserLogin.class).findFirst();
            if (country.getUrl().getCart() != null){
                Intent intent = new Intent(getActivity(), BaseWebViewActivity.class);
                intent.putExtra("url", country.getUrl().getCart());
                intent.putExtra("header", userLogin.getWebToken());
                intent.putExtra("title", getActivity().getString(R.string.text51));
                intent.putExtra("hasCart", true);
                startActivity(intent);
            }else{
                Toast.makeText(getActivity(), getActivity().getString(R.string.text46), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void getUserCart(String countryID){

        if (NetworkUtil.checkEnabledInternet(getActivity())) {
            //showProgress()
            UserLogin userLogin = Realm.getDefaultInstance().where(UserLogin.class).findFirst();
            RestClient restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient.class);
            Call<UserCart> call = restClient.getUserCart(userLogin.getToken(), ConfigUtil.getLocaleISO639(), countryID);
            call.enqueue(new Callback<UserCart>() {
                @Override
                public void onResponse(Call<UserCart> call, retrofit2.Response<UserCart> response) {
                    UserCart userCart = response.body();
                    if (userCart != null) {
                        if(userCart.getBlstatus() == ServerConstants.AUTH_TOKEN_EXPIRED || userCart.getBlstatus() == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(getActivity());
                            return;
                        }
                        if (userCart.getBlstatus() == ServerConstants.NO_ERROR) {
                            if (userCart.getProducts() != null){
                                Realm.getDefaultInstance().executeTransaction(realm -> userLogin.setQtyCartProducts(userCart.getProducts()));
                                //tvCartQty.setText(userCart.getProducts().toString());
                            }else{
                                Realm.getDefaultInstance().executeTransaction(realm -> userLogin.setQtyCartProducts(0));
                                Toast.makeText(getActivity(), getActivity().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(getActivity(), userCart.getBlmessage(), Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(getActivity(), getActivity().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UserCart> call, Throwable t) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.server_error), Toast.LENGTH_LONG).show();

                }
            });
        }else{
            Toast.makeText(getActivity(), getActivity().getString(R.string.server_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBrowseFragmentInteractionListener) {
            mListener = (OnBrowseFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBrowseFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public boolean canGoBack() {
        return mWebsiteContent != null && mWebsiteContent.canGoBack();
    }

    public void goBack() {
        if (mWebsiteContent != null) {
            mWebsiteContent.goBack();
        }
    }

    /**
     * Finds the UI elements matching a member field of this instance and assigns them to their
     * respective field
     */
    private void mapUiElements(View view) {
        alertDialogHelper = new AlertDialogHelper(getActivity());
        ibCloseView = view.findViewById(R.id.ibCloseView);
        llCartTool = view.findViewById(R.id.llCartTool);
        tvCartQty = (TextView) view.findViewById(R.id.tvCartQty);
        mWebsiteContent = (WebView) view.findViewById(R.id.browse_wv_website_content);
        mWebsiteProgressBar = (ProgressBar) view.findViewById(R.id.browse_pb_website_progress);
        mChooseStoreButton = view.findViewById(R.id.browse_toolbar_tv_choose_store);
        mBrowseBackButton = (ImageButton) view.findViewById(R.id.browse_toolbar_ib_back);
        mBrowseForwardButton = (ImageButton) view.findViewById(R.id.browse_toolbar_ib_forward);
        mQuestionModalButton = (ImageButton) view.findViewById(R.id.browse_toolbar_question);
        mCartButton = (ImageButton) view.findViewById(R.id.browse_toolbar_cart);
        mQuoteBottomSheet = view.findViewById(R.id.browse_ll_quote_bottom_sheet);
        mBackgroundOverlay = view.findViewById(R.id.browse_view_background_overlay);
        mBrowseToolbarOverlay = view.findViewById(R.id.browse_toolbar_view_overlay);
        mQuoteBottomSheetMarginTop = view.findViewById(R.id.browse_view_quote_bottom_sheet_header_margin);
        mQuoteBottomSheetHeader = view.findViewById(R.id.browse_view_quote_bottom_sheet_header);
        mQuoteBottomSheetHeaderProgressBar = (ProgressBar) view.findViewById(R.id.browse_pb_quote_bottom_sheet_header_progress_bar);
        mQuoteBottomSheetHeaderText = (TextView) view.findViewById(R.id.browse_tv_quote_bottom_sheet_header_text);
        mQuoteBottomSheetHeaderArrow = (ImageView) view.findViewById(R.id.browse_iv_quote_bottom_sheet_header_arrow);
        mQuoteBottomSheetPricesGrid = (GridLayout) view.findViewById(R.id.browse_gl_quote_bottom_sheet_prices);
        mQuoteBottomSheetMarginTopMaxHeight = GraphicUtils.dpToPx(getActivity(), BOTTOM_SHEET_MARGIN_TOP_MAX_HEIGHT_DP);
        mBuyButton = (Button) view.findViewById(R.id.browse_btn_buy);
        mBuyProgressBar = (ProgressBar) view.findViewById(R.id.browse_pb_buy);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        //mBuyProgressBar.setBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
        mChooseStoreButton.setText(storeName.toUpperCase());
        ibCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getActivity().finish();
                EventBus.getDefault().post(new SelectStoreEvent(true));
            }
        });
    }

    /**
     * Sets up the required configurations and callbacks to allow the correct functioning of the bottom sheet
     */
    private void configureQuoteBottomSheet() {

        mQuoteBottomSheetBehavior = BottomSheetBehavior.from(mQuoteBottomSheet);
        final View.OnClickListener collapseBottomSheetClickListener =
                v -> mQuoteBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mQuoteBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // Make the background and the toolbar overlay not clickable, in order
                    // to bypass click events through them to the WebView or the Toolbar, respectively
                    mBackgroundOverlay.setOnClickListener(null);
                    mBackgroundOverlay.setClickable(false);

                    mBrowseToolbarOverlay.setOnClickListener(null);
                    mBrowseToolbarOverlay.setClickable(false);

                    rotateHeaderArrow(-180,0);

                } else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    // Allow dragging of the bottom sheet only when the product has already been quoted
                    if (mCurrentBrowseState == State.PRODUCT_QUOTED) {
                        // Make the background and the toolbar overlay clickable, in order to prevent
                        // click events from passing through them to the WebView or the Toolbar, respectively
                        mBackgroundOverlay.setOnClickListener(collapseBottomSheetClickListener);
                        mBrowseToolbarOverlay.setOnClickListener(collapseBottomSheetClickListener);
                    } else {
                        mQuoteBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (mWarning != null) {
                        GraphicUtils.showDialog(getActivity(), getString(R.string.caution), mWarning);
                        mWarning = null;
                    }

                    rotateHeaderArrow(0, -180);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // When sliding the bottom sheet, gradually alter the background's and the toolbar's overlays' opacity.
                mBrowseToolbarOverlay.setVisibility(View.VISIBLE);
                mBrowseToolbarOverlay.setAlpha(slideOffset);

                mBackgroundOverlay.setVisibility(View.VISIBLE);
                mBackgroundOverlay.setAlpha(slideOffset);

                int newHeight = Math.round(mQuoteBottomSheetMarginTopMaxHeight * slideOffset);

                // Also, alter the height of bottom sheet's top margin view
                mQuoteBottomSheetMarginTop.getLayoutParams().height = newHeight;
                mQuoteBottomSheetMarginTop.requestLayout();
            }

            private void rotateHeaderArrow(float fromDegrees, float toDegrees){
                RotateAnimation rotateAnimation = new RotateAnimation(
                        fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(150);
                rotateAnimation.setInterpolator(new LinearInterpolator());
                rotateAnimation.setFillAfter(true);
                mQuoteBottomSheetHeaderArrow.startAnimation(rotateAnimation);
            }
        });
    }

    /**
     * Sets the required parameters and setting required to load a website on {@link BrowseFragment#mWebsiteContent}
     * showing a progress bar
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        // Configure progress bar
        mWebsiteProgressBar.setProgress(0);
        mWebsiteProgressBar.setMax(100);
        mWebsiteContent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                stopStatusCheck();
                // Every time a URL change starts, reset the state and the current product
                resetProduct();
                setState(State.LOADING);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //handleNewUrl(url);
                // Check the state of the page every second
                if(storeName.equals(StoreManager.Store.BHPHOTO)){
                    mWebsiteContent.evaluateJavascript("$('#a2c_section').remove();$('.icon-menu_large').remove();$('.header-menu-item').hide();$('.welcome-region').hide();$('.icon-cart').remove();$('#live-chat-dummy-btn').remove();$('.experts').hide();$('#product_reviews').remove();$('#open-menu').remove();$('.notification-a').remove();$('.banners-region').hide();$('.store-pickup-banner-region').hide();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {

                            Log.d("LogReceived", s);
                        }
                    });

                    mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BHPHOTO');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BHPHOTO');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='2px solid #009750';el.style.padding='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('a2c_section');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';}", new ValueCallback<String>() {
                        public void onReceiveValue(String s) {
                            Log.d("LogReceived", s);

                        }
                    });


                }else {


                    mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BLAmazonAlert');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BLAmazonAlert');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='2px solid #009750';el.style.padding='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('addToCart');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';}", new ValueCallback<String>() {
                        public void onReceiveValue(String s) {

                        }
                    });

                    mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BLEbayAlert');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BLEbayAlert');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='1px solid #009750';el.style.padding='10px';el.style.marginTop='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('buyItNowBtn');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';var addCart = document.getElementById('addToCartBtn');addCart.style.display='none';var favBtn = document.getElementById('watchSignedOutBtn');favBtn.style.display='none';}", new ValueCallback<String>() {
                        public void onReceiveValue(String s) {

                        }
                    });
                }
                mStatusCheckTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        checkState();
                    }
                };
                mStatusCheckTimer = new Timer();
                mStatusCheckTimer.scheduleAtFixedRate(mStatusCheckTimerTask, 0, 1000);
                updateBrowserButtonsState();
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                try {
                    String host = Uri.parse(url).getHost();
                    String[] splitHost = new String[0];
                    if (host != null) {
                        splitHost = host.split("\\.");
                    }
                    String domain = splitHost[splitHost.length - 2] + "." + splitHost[splitHost.length - 1];
                    String storeDomain = mProductPageParser.getStoreDomain();
                    if (domain.equals(storeDomain)) {
                        return false;
                    } else {
                        if (!storeName.equals(StoreManager.Store.BHPHOTO)){
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name),String.format(getString(R.string.browse_only_store_domain_allowed), storeDomain),getString(R.string.accept_dialog),"","",-1, false);

                            return true;
                        }else {
                            return false;
                        }

                    }
                } catch (Exception e){
                    return false;
                }

            }


            @Override
            public void onPageCommitVisible(WebView view, String url) {
                //updateBrowserButtonsState();
            }
        });

        mWebsiteContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Update the progress bar as the page loads
                mWebsiteProgressBar.setProgress(progress);
                // Hide the progress bar upon finishing the page load
                mWebsiteProgressBar.setVisibility(progress == 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Enable JS
        WebSettings webViewSettings = mWebsiteContent.getSettings();
        webViewSettings.setJavaScriptEnabled(true);

        // Enable local storage
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setDatabaseEnabled(true);
    }

    private void handleNewUrl(String url) {
        Uri uri = Uri.parse(url);

        if (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
            openExternalWebsite(url);
        else if (uri.getScheme().equals("intent")) {
            String appPackage = getAppPackageFromUri(uri);

            if (appPackage != null) {
                PackageManager manager = getContext().getPackageManager();
                Intent appIntent = manager.getLaunchIntentForPackage(appPackage);

                if (appIntent != null) {
                    getActivity().startActivity(appIntent);
                } else {
                    openExternalWebsite("https://play.google.com/store/apps/details?id=" + appPackage);
                }
            }
        }
    }

    private String getAppPackageFromUri(Uri intentUri) {
        Pattern pattern = Pattern.compile("package=(.*?);");
        Matcher matcher = pattern.matcher(intentUri.getFragment());

        if (matcher.find())
            return matcher.group(1);

        return null;
    }

    private void openExternalWebsite(String url) {
        Intent webeIntent = new Intent(Intent.ACTION_VIEW);
        webeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webeIntent.setData(Uri.parse(url));
        getActivity().startActivity(webeIntent);
    }

    /**
     * Enable or disable de back and forward buttons depending on the availability of pages in
     * the respective stacks
     */
    private void updateBrowserButtonsState() {

        if (mWebsiteContent != null) {
            if (mWebsiteContent.canGoBack()) {
                mBrowseBackButton.setClickable(true);
                mBrowseBackButton.setImageResource(R.drawable.ic_arrow_back_white);
            } else {
                mBrowseBackButton.setClickable(false);
                mBrowseBackButton.setImageResource(R.drawable.ic_arrow_back_disabled);
            }
            if (mWebsiteContent.canGoForward()) {
                mBrowseForwardButton.setClickable(true);
                mBrowseForwardButton.setImageResource(R.drawable.ic_arrow_forward);
            } else {
                mBrowseForwardButton.setClickable(false);
                mBrowseForwardButton.setImageResource(R.drawable.ic_arrow_forward_disabled);
            }
        }
    }

    /**
     * Checks and assigns the state of the current fragment
     */
    private void checkState() {
        // Do not check while there is an ongoing check.
        // This method is called sequentially multiple times by the same thread so there is no risk
        // of multiple threads triggering an inconsistent state. Nevertheless, it could happen that
        // a check has just finished and mCheckingState has not been updated yet. This is no a major
        // issue since the check will be performed anyway in the next iteration.
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("storeName", storeName);

                    mWebsiteContent.evaluateJavascript("$('#vi-msku__select-box-1000').val()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            String diff = s.replace("\"", "");

                            if (diff.equals("-1")){
                                setState(State.DIFFERENTIATORS_MISSING);
                                hasNewDiferenciator = false;

                            }else {
                                if (!s.equals(diferentiatorValue) || diferentiatorValue.equals("")){
                                    diferentiatorValue = s;
                                    Log.e("diferentiatorValue", diferentiatorValue);
                                    if (!diferentiatorValue.equals("null")){
                                        hasNewDiferenciator = true;

                                        Log.e("receivedd", diferentiatorValue.replace("\"", ""));
                                    }

                                }
                            }
                            Log.d("LogReceived", s);
                        }
                    });

                }

            });

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (storeName.equals(StoreManager.Store.BHPHOTO)){
                        if(mWebsiteContent.getUrl().contains("c/product")){
                            Log.e("hasProduct", "hasProduct");
                            mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BHPHOTO');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BHPHOTO');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='2px solid #009750';el.style.padding='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('a2c_section');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';}", new ValueCallback<String>() {
                                public void onReceiveValue(String s) {
                                    Log.d("LogBHPHOTOScript", s);
                                }
                            });
                            if (!bhURL.equals(mWebsiteContent.getUrl())){
                                bhURL = mWebsiteContent.getUrl();
                                mCurrentProductHash = mWebsiteContent.getUrl();
                                // Begin quoting the newly selected product
                                setState(State.QUOTING);
                                Log.e("newProductHash", mWebsiteContent.getUrl());
                                getQuoteFromServer(mWebsiteContent.getUrl());

                            }

                            //hasNewDiferenciator = false;
                        }else {
                            setState(State.NON_PRODUCT_PAGE);
                        }
                    }
                }
            });
        }


        if (!mCheckingState || hasNewDiferenciator) {
            mCheckingState = true;
            hasNewDiferenciator = false;
            if (getActivity() != null){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!storeName.equals(StoreManager.Store.BHPHOTO)){
                            mWebsiteContent.evaluateJavascript(
                                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                    new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String html) {
                                            try{
                                                if (html.contains("variationCombinations")){
                                                    String data = html.substring(html.indexOf("variationCombinations")).replace("\\", "");

                                                    int startIndex = 0;
                                                    int endIndex = 0;
                                                    for (int i = 0; i < data.length(); i++){

                                                        if (data.charAt(i) == '{'){
                                                            startIndex = i;
                                                            Log.e("startIndex", startIndex + "");
                                                        }
                                                        if (data.charAt(i) == '}'){
                                                            endIndex = i;
                                                            Log.e("endIndex", endIndex + "");
                                                        }
                                                        if ((startIndex != 0 && endIndex != 0) && (endIndex < startIndex)){
                                                            String json = data.substring(startIndex, endIndex + 1);
                                                            json = json.substring(1, json.length() - 1);
                                                            String[] keyValuePairs = json.split(",");
                                                            differencitorMap = new HashMap<>();

                                                            for(String pair : keyValuePairs) {
                                                                String[] entry = pair.split(":");
                                                                differencitorMap.put(entry[0].trim(), entry[1].trim());
                                                            }
                                                            Log.e("map", differencitorMap.toString());
                                                            return;

                                                        }
                                                    }
                                                    Log.e("hasData", data);

                                                }else {
                                                    Log.e("hasData", "false");

                                                }
                                            }catch (Exception e){
                                                Log.e("indexError", e.toString());
                                            }
                                        }
                                    });
                        }

                    }
                });
            }
            // Receive and update the product hash
            final ProductPageParser.OnRequestStringResultListener productHashListener =
                    new ProductPageParser.OnRequestStringResultListener() {
                        @Override
                        public void onResult(@NonNull String newProductHash) {
                            if (!storeName.equals(StoreManager.Store.BHPHOTO)){
                                if (!newProductHash.equals(mCurrentProductHash) || hasNewDiferenciator) {

                                    // Save the current product hash when it is different than the last one.
                                    mCurrentProductHash = newProductHash;
                                    // Begin quoting the newly selected product
                                    setState(State.QUOTING);
                                    Log.e("newProductHash", newProductHash);
                                    getQuoteFromServer(newProductHash);
                                    hasNewDiferenciator = false;
                                } else {
                                    hasNewDiferenciator = false;
                                    mCheckingState = false;
                                }
                            }
                        }
                    };

            // Check if the product page has all of its differentiators selected
            final ProductPageParser.OnCheckResultListener allDifferentiatorsSelectedListener =
                    new ProductPageParser.OnCheckResultListener() {
                        @Override
                        public void onResult(boolean allDifferentiatorsSelected) {
                            // Fragment might be removed while waiting for this response, so isAdded() should be checked
                            if (isAdded()) {
                                if (!allDifferentiatorsSelected && !StoreManager.Store.BHPHOTO.equals(storeName)) {
                                    // Directly set the state when the current page requires the selection of differentiators
                                    setState(State.DIFFERENTIATORS_MISSING);
                                    mCheckingState = false;
                                } else {
                                    //setState(State.DIFFERENTIATORS_MISSING);
                                    mProductPageParser.requestProductHash(mWebsiteContent, productHashListener);
                                }
                            }
                        }
                    };

            // Check if the page is a product page
            final ProductPageParser.OnCheckResultListener isProductPageListener =
                    new ProductPageParser.OnCheckResultListener() {
                        @Override
                        public void onResult(boolean isProductPage) {
                            // Fragment might be removed while waiting for this response, so isAdded() should be checked
                            if (isAdded()) {
                                if (!isProductPage && !StoreManager.Store.BHPHOTO.equals(storeName)) {
                                    // Directly set the state when the current page is not a product
                                    setState(State.NON_PRODUCT_PAGE);
                                    mCheckingState = false;
                                } else {
                                    mProductPageParser.checkIfAllDifferentiatorsSelected(mWebsiteContent, allDifferentiatorsSelectedListener);
                                }
                            }
                        }
                    };
            // Asynchronously perform the required checks, starting by checking if the page represents a product
            mProductPageParser.checkIfProductPage(mWebsiteContent, isProductPageListener);
        }
    }

    private void getQuoteFromServer( String productCode){
        @StoreManager.Store final String store = getArguments().getString(ARG_STORE);
        Log.e("store", store);
        if (NetworkUtil.checkEnabledInternet(getActivity()) && store != null){
            //Log.e("diferentiatorValue", diferentiatorValue != null ? diferentiatorValue : "");
            //Log.e("value", differencitorMap.get(diferentiatorValue) != null ? differencitorMap.get(diferentiatorValue) : "");
            String difValue = "";
            if (differencitorMap != null){
                Log.e("receivedmap", differencitorMap.toString());
                difValue = differencitorMap.get(diferentiatorValue);
            }
            JsonObject jsonObject = new JsonObject();
            String storeID;
            if (store.equals(StoreManager.Store.EBAY)){
                storeID = "2";
            }else if (store.equals(StoreManager.Store.AMAZON)){
                storeID = "1";

            }else {
                storeID = "3";
            }
            jsonObject.addProperty("storeId", storeID);
            jsonObject.addProperty("productCode",productCode);
            jsonObject.addProperty("productDiff",difValue);

            Log.e("jsonAddProduct", jsonObject.toString());

            UserLogin userLogin = Realm.getDefaultInstance().where(UserLogin.class).findFirst();
            RestClient restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient.class);
            Call<QuoteResult> call = restClient.getQuoteFromServer(userLogin.getToken(), ConfigUtil.getLocaleISO639(), jsonObject);
            call.enqueue(new Callback<QuoteResult>() {
                @Override
                public void onResponse(@NotNull Call<QuoteResult> call, @NotNull retrofit2.Response<QuoteResult> response) {
                    final QuoteResult quoteResult = response.body();
                    if (quoteResult != null){
                        if (quoteResult.getBlstatus() == ServerConstants.AUTH_TOKEN_EXPIRED || quoteResult.getBlstatus() == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(getActivity());
                            return;
                        }
                        if (quoteResult.getBlstatus() == 0){
                            if (isAdded()) {
                                if (quoteResult.getQuoteData() != null){
                                    JSONObject mJSONObject = null;
                                    String jsonInString = new Gson().toJson(quoteResult);
                                    try {
                                        mJSONObject = new JSONObject(jsonInString);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    Quotation quotation = Quotation.getQuotationFromJson(mJSONObject);
                                    Log.e("ShippingMethods", quotation.getShippingMethods().toString());
                                    //quotation.setSuccess(true);
                                    if (quotation.isSuccessful()) {
                                        setUpPricesGrid(quotation);

                                        // Save the warning, note that it might be null.
                                        mWarning = quotation.getWarning();

                                        // Set the correct state
                                        setState(State.PRODUCT_QUOTED);
                                        mCheckingState = false;

                                        mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BHPHOTO');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BHPHOTO');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='2px solid #009750';el.style.padding='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('a2c_section');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';}", new ValueCallback<String>() {
                                            public void onReceiveValue(String s) {
                                                Log.d("LogBHPHOTOScript", s);
                                            }
                                        });

                                        mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BLAmazonAlert');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BLAmazonAlert');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='2px solid #009750';el.style.padding='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('addToCart');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';}", new ValueCallback<String>() {
                                            public void onReceiveValue(String s) {

                                            }
                                        });

                                        mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BLAmazonAlert');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BLAmazonAlert');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='2px solid #009750';el.style.padding='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('addToCart');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';}", new ValueCallback<String>() {
                                            public void onReceiveValue(String s) {

                                            }
                                        });

                                        mWebsiteContent.evaluateJavascript("var elementExists = document.getElementById('BLEbayAlert');if(!elementExists){var el = document.createElement('div');el.setAttribute('id', 'BLEbayAlert');el.innerHTML = 'Para cotizar por <strong>BuscaLibre</strong> utiliza la barra verde que está en la parte inferior de tu pantalla.';el.style.backgroundColor='#fff';el.style.fontSize='large';el.style.color='#f60';el.style.border='1px solid #009750';el.style.padding='10px';el.style.marginTop='10px';el.style.textAlign='center';el.style.borderRadius='5px';var div = document.getElementById('buyItNowBtn');div.parentNode.insertBefore(el, div.nextSibling);div.style.display='none';var addCart = document.getElementById('addToCartBtn');addCart.style.display='none';var favBtn = document.getElementById('watchSignedOutBtn');favBtn.style.display='none';}", new ValueCallback<String>() {
                                            public void onReceiveValue(String s) {

                                            }
                                        });
                                        if(storeID.equals("1")){
                                            mWebsiteContent.evaluateJavascript(
                                                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                                    new ValueCallback<String>() {
                                                        @Override
                                                        public void onReceiveValue(String html) {
                                                            Log.e("HTMLAmazon", html);
                                                        }
                                                    });
                                        }
                                        if(storeID.equals("2")){
                                            mWebsiteContent.evaluateJavascript(
                                                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                                    new ValueCallback<String>() {
                                                        @Override
                                                        public void onReceiveValue(String html) {
                                                            try{
                                                                String image = html.substring(html.indexOf("image\\\" content=\\\""), html.lastIndexOf("\\\">\\")).replace("image\\\" content=\\\"", "");
                                                                Log.e("imageProduct", image);
                                                                int startIndex = 0;
                                                                int endIndex;
                                                                for (int i = 0; i < image.length(); i++){
                                                                    if (image.charAt(i) == '\\'){
                                                                        endIndex = i;
                                                                        Log.e("endIndex", endIndex + "");
                                                                        Log.e("urlImageFinal", image.substring(startIndex, endIndex));
                                                                        Picasso.get().load(image.substring(startIndex, endIndex)).into(ivProductImage);
                                                                        return;
                                                                    }
                                                                }
                                                            }catch (Exception e){
                                                                Log.e("indexError", e.toString());
                                                            }

                                                        }
                                                    });
                                        }


                                    } else {
                                        // Inform the error to the user and set the error state

                                        //handleQuotationError(quotation);
                                        setState(State.ERROR_QUOTING);
                                        mCheckingState = false;
                                    }
                                }
                            }
                            //stopStatusCheck();
                        }else {
                            //stopStatusCheck();
                            setState(State.ERROR_QUOTING);
                            mCheckingState = false;
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name),quoteResult.getBlmessage(),getString(R.string.accept_dialog),"","",-1, false);
                        }

                    }else {
                        //stopStatusCheck();
                        setState(State.ERROR_QUOTING);
                        mCheckingState = false;
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false);

                    }
                    setBuyButtonEnabled(true);
                }

                @Override
                public void onFailure(Call<QuoteResult> call, Throwable t) {
                    stopStatusCheck();
                    setState(State.ERROR_QUOTING);
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false);
                    setBuyButtonEnabled(true);
                    Log.e("addProductError", t.toString());
                    mCheckingState = false;
                }
            });
        }else {
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false);

        }
    }

    private void handleQuotationError(Quotation quotation) {

        String errorMessage = quotation.getError();
        @StoreManager.Store final String store = getArguments().getString(ARG_STORE);
        Log.d("errorMessage", errorMessage);
        //Log.d("store", store);
        //Log.d("product", mCurrentProductHash);
        //if(errorMessage) {
        if (quotation.getErrorCode() == 10) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle(getString(R.string.dialog_cotiza_email_title));
            builder.setMessage(R.string.dialog_cotiza_email_body);


            final EditText input = new EditText(getContext());

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton(R.string.text25, (dialog, which) -> {
                String email = input.getText().toString();

                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url = "https://www.buscalibre.cl/v2/app-mail?codigo=" + mCurrentProductHash + "&sitio=" + store + "&email=" + email;

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getActivity(), R.string.accept_dialog, Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), R.string.text27, Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest);


            });
            builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();


        } else if (quotation.getErrorCode() != null) {
            errorMessage +=
                    "\n(" + getString(R.string.error_code) + ": " + quotation.getErrorCode() + ")";

            GraphicUtils.showDialog(
                    getActivity(),
                    getString(R.string.browse_error_quoting_title),
                    errorMessage
            );
        }
        //}

    }

    /**
     * Stops the timer and the task that checks for the current state of the fragment
     */
    private void stopStatusCheck() {
        if (mStatusCheckTimerTask != null) {
            mStatusCheckTimerTask.cancel();
        }
        if (mStatusCheckTimer != null) {
            mStatusCheckTimer.cancel();
        }
    }

    /**
     * Assigns interactions listeners (e.g. OnClickListeners) to UI elements that require it
     */
    private void setUiListeners() {
        setBrowserButtonsListeners();
        setBottomSheetListeners();
    }

    /**
     * Helper method for {@link #setUiListeners()}
     */
    private void setBrowserButtonsListeners(){
        View.OnClickListener onBrowseButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == mBrowseBackButton) {
                    mWebsiteContent.goBack();
                } else if (v == mBrowseForwardButton) {
                    mWebsiteContent.goForward();
                } else if (v == mChooseStoreButton) {
                    stopStatusCheck();
                    mListener.onChooseStoreSelected();
                } else if (v == mCartButton) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(mBLCart + mAnalyticsKeyCart));
                    startActivity(browserIntent);
                } else if (v == mQuestionModalButton) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.text28);
                    LinearLayout layout = new LinearLayout(getContext());
                    layout.setOrientation(LinearLayout.VERTICAL);

                    // Set Email properties
                    final EditText input = new EditText(getContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setHint(R.string.placeholder_email);
                    layout.addView(input);

                    // Set Consulta properties
                    final EditText inputConsulta = new EditText(getContext());
                    inputConsulta.setSingleLine(false);  //add this
                    inputConsulta.setLines(4);
                    inputConsulta.setMaxLines(5);
                    inputConsulta.setGravity(Gravity.LEFT | Gravity.TOP);
                    inputConsulta.setHorizontalScrollBarEnabled(false); //this
                    inputConsulta.setHint(R.string.placeholder_consulta);
                    layout.addView(inputConsulta);
                    builder.setView(layout);

                    // Set up the buttons
                    builder.setPositiveButton(R.string.accept_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("Email", input.getText().toString());
                            Log.d("Consulta", inputConsulta.getText().toString());
                            String email = input.getText().toString();
                            String consulta = inputConsulta.getText().toString();
                            // Instantiate the RequestQueue.
                            RequestQueue queue = Volley.newRequestQueue(getContext());
                            String url = "https://www.buscalibre.cl/v2/app-comment?email="+email+"&comentario="+consulta;
                            // Request a string response from the provided URL.
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            // Display the first 500 characters of the response string.
                                            //mTextView.setText("Response is: "+ response.substring(0,500));
                                            Toast.makeText(getActivity(), R.string.text29, Toast.LENGTH_SHORT).show();
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //mTextView.setText("That didn't work!");
                                    Toast.makeText(getActivity(), R.string.text30, Toast.LENGTH_SHORT).show();
                                }
                            });
                            // Add the request to the RequestQueue.
                            queue.add(stringRequest);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        };
        mBrowseBackButton.setOnClickListener(onBrowseButtonClickListener);
        mBrowseForwardButton.setOnClickListener(onBrowseButtonClickListener);
        mChooseStoreButton.setOnClickListener(onBrowseButtonClickListener);
        mQuestionModalButton.setOnClickListener(onBrowseButtonClickListener);
        mCartButton.setOnClickListener(onBrowseButtonClickListener);
    }

    /**
     * Helper method for {@link #setUiListeners()}
     */
    private void setBottomSheetListeners() {
        // Set up bottom sheet header click listener for showing the quotation
        mQuoteBottomSheetHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentBrowseState == State.PRODUCT_QUOTED) {
                    // If the bottom sheet is collapsed or settling, expand it. Collapse it otherwise
                    int bottomSheetState = mQuoteBottomSheetBehavior.getState();
                    mQuoteBottomSheetBehavior.setState(
                            bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED ||
                                    bottomSheetState == BottomSheetBehavior.STATE_SETTLING ?
                                    BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED
                    );
                }
            }
        });

        mBuyButton.setOnClickListener(v -> {
            setBuyButtonEnabled(false);
            if (mSelectedPrice != null && mCurrentProductHash != "") {

                Log.e("mCurrentProductHash", mCurrentProductHash);
                Log.e("getCondition", mSelectedPrice.getCondition());
                Log.e("getShippingMethodKind", mSelectedPrice.getShippingMethodKind());
                @StoreManager.Store final String store = getArguments().getString(ARG_STORE);
                Log.e("store", store);
                String storeID;
                if (store.equals(StoreManager.Store.EBAY)){
                    storeID = "2";
                }else if (store.equals(StoreManager.Store.AMAZON)){
                    storeID = "1";

                }else {
                    storeID = "3";
                }
                addProduct(storeID, mCurrentProductHash, "",mSelectedPrice.getCondition(),mSelectedPrice.getShippingMethodKind());

            }
        });
    }

    private void addProduct(String storeId, String productCode, String productDiff, String condition,
                            String courier){

        if (NetworkUtil.checkEnabledInternet(getActivity())){

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("storeId",storeId);
            jsonObject.addProperty("productCode",productCode);
            jsonObject.addProperty("productDiff",productDiff);
            jsonObject.addProperty("condition",condition);
            jsonObject.addProperty("courier", courier);
            Log.e("jsonAddProduct", jsonObject.toString());
            UserLogin userLogin = Realm.getDefaultInstance().where(UserLogin.class).findFirst();
            RestClient restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient.class);
            Call<CartResult> call = restClient.getCartFromServer(userLogin.getToken(), ConfigUtil.getLocaleISO639(), jsonObject);
            call.enqueue(new Callback<CartResult>() {
                @Override
                public void onResponse(@NotNull Call<CartResult> call, @NotNull retrofit2.Response<CartResult> response) {
                    final CartResult cartResult = response.body();
                    if (cartResult != null){
                        if (cartResult.getBlstatus() == ServerConstants.AUTH_TOKEN_EXPIRED || cartResult.getBlstatus() == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(getActivity());
                            return;
                        }
                        if (cartResult.getBlstatus() == 0){
                            Intent browserIntent = new Intent(getActivity(), BaseWebViewActivity.class);
                            browserIntent.putExtra("url", Uri.parse(cartResult.getCartURI()).toString());
                            browserIntent.putExtra("header", userLogin.getWebToken());
                            browserIntent.putExtra("title", getString(R.string.text39));
                            startActivity(browserIntent);
                            Log.d("STOREURL", Uri.parse(cartResult.getCartURI()).toString());
                        }else {
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name),cartResult.getBlmessage(),getString(R.string.accept_dialog),"","",-1, false);
                        }

                    }else {
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false);

                    }
                    setBuyButtonEnabled(true);
                }

                @Override
                public void onFailure(Call<CartResult> call, Throwable t) {
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false);
                    setBuyButtonEnabled(true);
                    Log.e("addProductError", t.toString());
                }
            });
        }else {
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false);
        }
    }


    /**
     * Enables or disables the buy button, updating the UI accordingly
     */
    private void setBuyButtonEnabled(boolean enabled) {
        mBuyButton.setEnabled(enabled);
        mBuyButton.setText(enabled ? getResources().getString(R.string.browse_buy) : "");
        mBuyProgressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    /**
     * Assigns a new state to the current fragment, updating the UI accordingly
     *
     * @param newState
     */
    private void setState(@State int newState) {
        // Automatically assign the new state since all transitions between any pair of states are valid.
        mCurrentBrowseState = newState;

        switch (newState) {
            case State.LOADING:
                updateBottomSheetHeader(R.color.colorPrimary, "", true, false);
                break;
            case State.NON_PRODUCT_PAGE:
                updateBottomSheetHeader(R.color.colorPrimary, getString(R.string.browse_choose_a_product), false, false);
                break;

            case State.DIFFERENTIATORS_MISSING:
                updateBottomSheetHeader(R.color.colorPrimary, getString(R.string.browse_choose_differentiators), false, false);
                break;

            case State.QUOTING:
                updateBottomSheetHeader(R.color.colorPrimary, getString(R.string.browse_quoting), true, false);
                break;

            case State.PRODUCT_QUOTED:
                updateBottomSheetHeader(R.color.colorShowQuoteHeader, getString(R.string.browse_view_quote), false, true);
                break;

            case State.ERROR_QUOTING:
                updateBottomSheetHeader(R.color.colorPrimary, getActivity().getString(R.string.browse_problem_quoting), false, false);
                break;
        }
    }

    /**
     * Deletes all product-related data, updating the UI accordingly
     */
    private void resetProduct() {
        mCurrentProductHash = "";
        mWarning = null;
        mSelectedPrice = null;
        final int childCount = mQuoteBottomSheetPricesGrid.getChildCount();
        if (getActivity() != null){
            getActivity().runOnUiThread(() -> {
                for (int i = 0; i < childCount; ++i) {
                    View child = mQuoteBottomSheetPricesGrid.getChildAt(i);
                    if (child instanceof PriceView) {
                        ((PriceView) child).reset();
                    }
                }
            });
        }
    }

    /**
     * Updates the bottom sheet header in the ui thread
     *
     * @param headerColorId   Resource id of the header color to set
     * @param headerText      Text to set in the header
     * @param showProgressBar True if a progress bar should be shown next to the header text
     */
    private void updateBottomSheetHeader(final int headerColorId, final String headerText, final boolean showProgressBar, final boolean showArrow) {
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mQuoteBottomSheetMarginTop.getLayoutParams().height = 0;
                    mQuoteBottomSheetMarginTop.requestLayout();
                    mQuoteBottomSheetHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), headerColorId));
                    mQuoteBottomSheetHeaderProgressBar.setVisibility(showProgressBar ? View.VISIBLE : View.GONE);
                    mQuoteBottomSheetHeaderText.setText(headerText);
                    mQuoteBottomSheetHeaderArrow.clearAnimation();
                    mQuoteBottomSheetHeaderArrow.setVisibility(showArrow ? View.VISIBLE : View.INVISIBLE);
                    mQuoteBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
        }
    }

    /**
     * Set ups the prices and selection of price handling
     *
     * @param quotation {@link Quotation} used to obtain the prices and delivery time
     */
    private void setUpPricesGrid(final Quotation quotation) {
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Fill the prices in the grid
                    fillPriceViewsData(quotation);
                    // Once all the prices have been assigned, iterate over the grid to pre-select the fastest shipping method
                    selectSuggestedPriceOption();
                    // Then, assign click listeners to every available children
                    assignPricesClickListeners();
                    // Note: All of the 3 preceding calls iterate in some way over #mQuoteBottomSheetPricesGrid children.
                    // Despite the functionality of each method could be refactored in one single method to avoid multiple
                    // loops through the children, it would make the code less maintainable for a negligible performance gain
                }
            });
        }
    }

    /**
     * Helper method for {@link #setUpPricesGrid(Quotation)}. Fills the price and delivery time of every
     * shipping method in {@link #mQuoteBottomSheetPricesGrid} that is present in the received {@link Quotation}
     *
     * @param quotation {@link Quotation} used to obtain the prices and delivery time
     */
    private void fillPriceViewsData(Quotation quotation) {

        Map<String, Map<String, ShippingMethod>> shippingMethods = quotation.getShippingMethods();
        Log.e("shippingMethods", shippingMethods.toString());
        for (Map.Entry<String, Map<String, ShippingMethod>> conditionEntry : shippingMethods.entrySet()) {
            String condition = conditionEntry.getKey();
            Map<String, ShippingMethod> currentConditionShippingMethods = conditionEntry.getValue();
            for (Map.Entry<String, ShippingMethod> shippingMethodKindEntry : currentConditionShippingMethods.entrySet()) {
                String shippingMethodKind = shippingMethodKindEntry.getKey();
                final ShippingMethod shippingMethod = shippingMethodKindEntry.getValue();

                Integer childPosition = getPricesGridPosition(condition, shippingMethodKind);
                if (childPosition != null) {
                    final PriceView priceView = (PriceView) mQuoteBottomSheetPricesGrid.getChildAt(childPosition);
                    priceView.setUp(
                            getActivity(),
                            shippingMethod.isAvailable(),
                            shippingMethod.getPrice(),
                            shippingMethod.getMinArrivalTime(),
                            shippingMethod.getMaxArrivalTime(),
                            condition,
                            shippingMethodKind,
                            false
                    );
                }

            }
        }
    }

    /**
     * Select the first price found using the following order criteria:
     * - First, the fastest shipping method
     * - Second, the best condition of the product
     */
    private void selectSuggestedPriceOption() {
        final int columnCount = mQuoteBottomSheetPricesGrid.getColumnCount();
        final int rowCount = mQuoteBottomSheetPricesGrid.getRowCount();

        // Skip the zeroth row and column as they both are headers.
        for (int column = 1; column < columnCount; ++column) {
            for (int row = 1; row < rowCount; ++row) {
                int position = columnCount * row + column;
                PriceView currentPrice = (PriceView) mQuoteBottomSheetPricesGrid.getChildAt(position);
                if (currentPrice.isAvailable()) {
                    mSelectedPrice = currentPrice;
                    mSelectedPrice.select();
                    return;
                }
            }
        }
    }

    /**
     *
     */
    private void assignPricesClickListeners() {

        View.OnClickListener priceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPrice != null) {
                    mSelectedPrice.deselect();
                }
                PriceView newPrice = (PriceView) v;
                mSelectedPrice = newPrice;
                mSelectedPrice.select();
            }
        };

        final int columnCount = mQuoteBottomSheetPricesGrid.getColumnCount();
        final int rowCount = mQuoteBottomSheetPricesGrid.getRowCount();

        // Skip the zeroth row and column as they both are headers.
        for (int column = 1; column < columnCount; ++column) {
            for (int row = 1; row < rowCount; ++row) {
                int position = columnCount * row + column;
                PriceView currentPrice = (PriceView) mQuoteBottomSheetPricesGrid.getChildAt(position);
                if (currentPrice.isAvailable()) {
                    currentPrice.setOnClickListener(priceClickListener);
                }
            }
        }
    }

    /**
     * Returns the position of a price child of {@link #mQuoteBottomSheetPricesGrid}
     *
     * @param condition          Name of the condition of the child to look up
     * @param shippingMethodKind Kind of the shipping method of the child to look up
     * @return The linear position of the price in the grid, or null if the received arguments do
     * not correspond to a price position
     */
    private Integer getPricesGridPosition(String condition, String shippingMethodKind) {
        int row;
        switch (condition) {
            case Quotation.CONDITION_NEW:
                row = ROW_CONDITION_NEW;
                break;
            case Quotation.CONDITION_NEW_PRIME:
                row = ROW_CONDITION_NEW_PRIME;
                break;
            case Quotation.CONDITION_USED:
                row = ROW_CONDITION_USED;
                break;
            case Quotation.CONDITION_REFURBISHED:
                row = ROW_CONDITION_REFURBISHED;
                break;
            default:
                return null;
        }

        int column;
        switch (shippingMethodKind) {
            case Quotation.SHIPPING_METHOD_AIRPLANE:
                column = COLUMN_SHIPPING_METHOD_AIRPLANE;
                break;
            case Quotation.SHIPPING_METHOD_SHIP:
                column = COLUMN_SHIPPING_METHOD_SHIP;
                break;
            default:
                return null;
        }

        int totalColumns = mQuoteBottomSheetPricesGrid.getColumnCount();
        return totalColumns * row + column;
    }

    @Override
    public void onPositiveClick(int from) {

    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }

    /**
     * Defines an enum annotation for the possible states of this fragment.
     * This is preferred over regular enums in android.
     * References:
     * https://developer.android.com/studio/write/annotations.html#enum-annotations
     * https://stackoverflow.com/a/45175692/370798
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({State.LOADING, State.NON_PRODUCT_PAGE, State.DIFFERENTIATORS_MISSING, State.QUOTING, State.PRODUCT_QUOTED, State.ERROR_QUOTING})
    public @interface State {
        // Possible States (note that by definition these fields are public, static and final)
        int LOADING = 1;
        int NON_PRODUCT_PAGE = 2;
        int DIFFERENTIATORS_MISSING = 3;
        int QUOTING = 4;
        int PRODUCT_QUOTED = 5;
        int ERROR_QUOTING = 6;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnBrowseFragmentInteractionListener {
        void onChooseStoreSelected();
    }
}
