package com.siongu.sucker

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.siongu.sucker.annotation.annotations.SuckClick
import com.siongu.sucker.annotation.annotations.SuckView
import org.jetbrains.anko.toast

class MainActivity : Activity() {

    @SuckView(R.id.btn1)
    @JvmField
    var btn1: Button? = null
    @SuckView(R.id.btn2)
    @JvmField
    var btn2: Button? = null
    @SuckView(R.id.btn3)
    @JvmField
    var btn3: Button? = null

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
