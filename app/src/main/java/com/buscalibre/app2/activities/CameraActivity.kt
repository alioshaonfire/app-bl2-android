package com.buscalibre.app2.activities

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.util.AlertDialogHelper
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_camera.*


private const val REQUEST_PERMISSION = 1

private var url = ""
private var isSellBook = false



class CameraActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        initViews()
        getDataFromIntent()
        if (!checkPermission()) {
            requestPermissionsDialog()
        }
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            url = intent.getStringExtra("url")!!
            isSellBook = intent.getBooleanExtra("isSellBook", false)

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(
                R.string.text21), getString(R.string.accept_dialog),"","",1, false)
        }
    }

    private fun initViews() {
        showBackButton()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        //alertDialogHelper = AlertDialogHelper(this@CameraActivity)
        barcodeView.decodeContinuous(callback)
        barcodeView.setStatusText(AppConstants.EMPTY_TEXT)
        tvToolbarTitle.text = getString(R.string.text105)

    }

    fun manualScanOnClick(view: View) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name))
        builder.setTitle(getString(R.string.text126))
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton(getString(R.string.accept_dialog),
            DialogInterface.OnClickListener { dialog, which ->

                goToNextActivity( input.text.toString())

            })
        builder.setNegativeButton(getString(R.string.cancel_dialog),
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text != null) {
                barcodeView.pause()
                goToNextActivity(result.text)
            } else {
                Toast.makeText(applicationContext, getString(R.string.text50), Toast.LENGTH_SHORT).show()
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private fun goToNextActivity(isbn:String){
        if(isSellBook){
            val intent = Intent(applicationContext, SellBookQuoteAnimActivity::class.java)
            intent.putExtra("isbn", isbn)
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(applicationContext, BaseWebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("header", userLogin?.webToken)
            intent.putExtra("hasCart", true)
            intent.putExtra("title", getString(R.string.text49))
            intent.putExtra("key", isbn)
            intent.putExtra("replace_const", AppConstants.SEARCH_BY_ISBN_CODE)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    /**
     * Read permissions
     */
    private fun checkPermission(): Boolean {
        val cameraPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return cameraPermissionResult == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Permission request
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this@CameraActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION)
    }

    /**
     * Capture permissions OnResult
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@CameraActivity,
                    R.string.request_permisses,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@CameraActivity,
                    R.string.permisses_not_accepted,
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }



    /**
     * Create a dialog for permissions request
     */
    private fun requestPermissionsDialog() {
        alertDialogHelper.showAlertDialog(
            resources.getString(R.string.app_name),
            resources.getString(R.string.text84),
            getString(R.string.accept_dialog),
            "",
            "",
            1,
            false
        )
    }

    override fun onPositiveClick(from: Int) {
        when (from) {
            1 -> requestPermission()
        }
    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }


}