package com.buscalibre.app2.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.buscalibre.app2.R
import com.buscalibre.app2.fragments.AudioBooksFragment
import com.buscalibre.app2.fragments.EbooksFragment
import com.buscalibre.app2.util.AlertDialogHelper
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_my_ebook_list.*

private val TAG = ""

class MyEbookListActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ebook_list)
        initViews()
        isStoragePermissionGranted()
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, getString(R.string.accept_permissions))
                true
            } else {
                Log.v(TAG, getString(R.string.revoke_permissions))
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, getString(R.string.accept_permissions))
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
        }
    }

    private fun initViews(){
        showBackButton()
        //showCart()
        alertDialogHelper = AlertDialogHelper(this@MyEbookListActivity)
        tvToolbarTitle.text = getString(R.string.my_ebooks)

        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add(getString(R.string.ebooks), EbooksFragment::class.java)
                .add(getString(R.string.audiobooks), AudioBooksFragment::class.java)
                .create()
        )
        viewpagerShowcase.adapter = adapter
        tabEbookReader.setViewPager(viewpagerShowcase)

    }

    override fun onPositiveClick(from: Int) {

    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }
}