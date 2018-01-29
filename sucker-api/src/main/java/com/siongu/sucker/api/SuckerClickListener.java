package com.siongu.sucker.api;

import android.view.View;

public abstract class SuckerClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        handleClick(v);
    }

    public abstract void handleClick(View v);
}
