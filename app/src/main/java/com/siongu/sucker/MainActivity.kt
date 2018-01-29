package com.siongu.sucker

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.siongu.sucker.annotation.annotations.SuckClick
import org.jetbrains.anko.toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @SuckClick(R.id.btn1)
    fun btn1Click(v: View) {
        toast("btn1Click")
    }

    @SuckClick(R.id.btn2, R.id.btn3)
    fun btn2Click(v: View) {
        toast("btn2Click")
    }
}
