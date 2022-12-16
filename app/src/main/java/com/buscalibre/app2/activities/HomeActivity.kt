package com.buscalibre.app2.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.*
import com.buscalibre.app2.fragments.*
import com.buscalibre.app2.fragments.BrowseFragment.OnBrowseFragmentInteractionListener
import com.buscalibre.app2.models.MessageList
import com.buscalibre.app2.models.UserCart
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.ConfigUtil
import com.buscalibre.app2.util.StoreManager
import com.skydoves.androidbottombar.BottomMenuItem
import com.skydoves.androidbottombar.OnMenuItemSelectedListener
import com.skydoves.androidbottombar.animations.BadgeAnimation
import com.skydoves.androidbottombar.forms.badgeForm
import com.skydoves.androidbottombar.forms.iconForm
import com.skydoves.androidbottombar.forms.titleForm
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : BaseActivity(), OnBrowseFragmentInteractionListener {

    private val CONTENT_VIEW_ID = 10101010
    private val HOME_FRAGMENT = 0
    private val MY_ACCOUNT_FRAGMENT = 1
    private val MY_SHOP = 2
    private val NAV_MENU = 3
    private var fragment:Fragment? = null
    private lateinit var cartMenu:BottomMenuItem
    private var qtyBadge = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        getUserCart(country!!.id)
        initViews()
        initBottomBar()
        //getInboxMessages(false)
    }

    private fun initViews(){
        hideToolbar()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshCartProduct(cartProductsEvent: CartProductsEvent) {
        qtyBadge = cartProductsEvent.qty
        initBottomBar()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun selectStoreFragment(selectStoreEvent: SelectStoreEvent) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        fragment = SelectStoreFragment()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment as SelectStoreFragment).addToBackStack("")
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun selectSearchByKeywords(searchByKeywordsEvent: SearchByKeywordsEvent) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        val searchData = Bundle()
        searchData.putString("url", searchByKeywordsEvent.url)
        searchData.putString("header", searchByKeywordsEvent.header)
        searchData.putBoolean("hasCart", searchByKeywordsEvent.hasCart)
        searchData.putString("title", searchByKeywordsEvent.title)
        fragment = SearchByKeywordsFragment()
        (fragment as SearchByKeywordsFragment).arguments = searchData
        fragmentTransaction.replace(R.id.fragmentContainer, fragment as SearchByKeywordsFragment).addToBackStack("")
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun selectAmazonFragment(selectAmazonEvent: SelectAmazonEvent) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        fragment = BrowseFragment.newInstance(StoreManager.Store.AMAZON)
        fragmentTransaction.replace(R.id.fragmentContainer, fragment as BrowseFragment).addToBackStack("")
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun selectEbayFragment(selectEbayEvent: SelectEbayEvent) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        fragment = BrowseFragment.newInstance(StoreManager.Store.EBAY)
        fragmentTransaction.replace(R.id.fragmentContainer, fragment as BrowseFragment).addToBackStack("")
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun selectNavMenuFragment(selectNavMenuEvent: SelectNavMenuEvent) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        fragment = NavMenuFragment()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment as NavMenuFragment)
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun selectPaymentMethod(selectPaymentMethodEvent: SelectPaymentMethodEvent) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        fragment = PaymentMethodsFragment()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment as PaymentMethodsFragment).addToBackStack("")
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun initBaseWebViewFragment(baseWebViewEvent: BaseWebViewEvent) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        val webData = Bundle()
        webData.putString("url", baseWebViewEvent.url)
        webData.putString("header", baseWebViewEvent.header)
        webData.putBoolean("hasCart", baseWebViewEvent.hasCart)
        webData.putString("title", baseWebViewEvent.title)
        webData.putString("key", baseWebViewEvent.key)
        webData.putString("replace_const", baseWebViewEvent.replace_const)

        fragment = BaseWebViewFragment()
        (fragment as BaseWebViewFragment).arguments = webData
        fragmentTransaction.replace(R.id.fragmentContainer, fragment as BaseWebViewFragment).addToBackStack("")
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun selectHomeMenu(selectHomeMenuEvent: SelectHomeMenuEvent){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragment != null){
            fragmentTransaction.remove(fragment!!)
        }
        fragment = MainMenuFragment()
        fragmentTransaction.replace(R.id.fragmentContainer,
            fragment as MainMenuFragment
        )
        fragmentTransaction.commit()
    }

    private fun getInboxMessages(isRefresh:Boolean){
        val messageNotRead = realm.where(MessageList::class.java).equalTo("read", false).findAll()
        //initDrawer(baseToolbar, messageNotRead?.size ?: 0, isRefresh)
    }

    private fun initBottomBar(){
        val titleForm = titleForm(this) {
            setTitleColor(Color.WHITE)
            setTitleActiveColorRes(R.color.white)
        }

        val iconForm = iconForm(this) {
            setIconSize(28)
        }

        val badgeForm = badgeForm(this) {
            setBadgeTextSize(9f)
            setBadgePaddingLeft(2)
            setBadgePaddingRight(2)
            setBadgeDuration(550)
        }

        cartMenu = BottomMenuItem(this)
            .setTitleForm(titleForm)
            .setIconForm(iconForm)
            .setBadgeForm(badgeForm)
            .setTitle(getString(R.string.my_shop_tab))
            .setBadgeColorRes(R.color.sky_blue_bl)
            .setBadgeAnimation(BadgeAnimation.FADE)
            .setBadgeText(userLogin?.qtyCartProducts.toString())
            .setIcon(R.drawable.ic_white_cart)
            .build()

        androidBottomBar.addBottomMenuItems(
            listOf(
                BottomMenuItem(this)
                    .setTitleForm(titleForm)
                    .setIconForm(iconForm)
                    .setBadgeForm(badgeForm)
                    .setTitle(getString(R.string.home_tab))
                    .setIcon(R.drawable.home)
                    .build(),

                BottomMenuItem(this)
                    .setTitleForm(titleForm)
                    .setIconForm(iconForm)
                    .setBadgeForm(badgeForm)
                    .setTitle(getString(R.string.my_account_tab))
                    .setIcon(R.drawable.ic_profile)
                    .build(),

                cartMenu,

                BottomMenuItem(this)
                    .setTitleForm(titleForm)
                    .setIconForm(iconForm)
                    .setBadgeForm(badgeForm)
                    .setTitle(getString(R.string.menu))
                    .setBadgeColorRes(R.color.white)
                    .setBadgeAnimation(BadgeAnimation.SCALE)
                    .setIcon(R.drawable.ic_baseline_menu)
                    .build()
            )
        )



        androidBottomBar.onMenuItemSelectedListener = object : OnMenuItemSelectedListener {
            override fun onMenuItemSelected(index: Int, bottomMenuItem: BottomMenuItem, fromUser: Boolean) {
                when(index){

                    HOME_FRAGMENT -> {
                        selectHomeMenu(SelectHomeMenuEvent(true))
                    }

                    MY_ACCOUNT_FRAGMENT -> {
                        val fragmentManager = supportFragmentManager
                        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                        if (fragment != null){
                            fragmentTransaction.remove(fragment!!)
                        }
                        fragment = MyAccountFragment()
                        fragmentTransaction.replace(R.id.fragmentContainer,
                            fragment as MyAccountFragment
                        )
                        fragmentTransaction.commit()
                    }

                    MY_SHOP -> {
                        val fragmentManager = supportFragmentManager
                        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                        if (fragment != null){
                            fragmentTransaction.remove(fragment!!)
                        }
                        fragment = CartFragment()
                        fragmentTransaction.replace(R.id.fragmentContainer, fragment as CartFragment)
                        fragmentTransaction.commit()
                    }

                    NAV_MENU -> {
                        selectNavMenuFragment(SelectNavMenuEvent(true))
                    }
                }
            }
        }

        androidBottomBar.setOnBottomMenuInitializedListener {
            androidBottomBar.showBadge(index = 2)
        }


    }
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onChooseStoreSelected() {

    }
}