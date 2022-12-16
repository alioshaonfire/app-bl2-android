package com.buscalibre.app2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.binpar.bibooks.sdk.SDKBibooks
import com.buscalibre.app2.R
import com.buscalibre.app2.models.MessageList
import kotlinx.android.synthetic.main.activity_message_detail.*

private var id = ""

class MediaPlayerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)
        initData()
    }

    private fun initData() {
        val bundle = intent.extras
        if(bundle != null){
            if (bundle.containsKey("id")){
                id = bundle.getString("id")!!
                Log.e("bookID", id)

            }
        }
    }
}