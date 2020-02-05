package com.lush_digital.objanimation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lush_digital.objanimation.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    LoadingFragment.newInstance()
                )
                .commitNow()
        }
    }
}
