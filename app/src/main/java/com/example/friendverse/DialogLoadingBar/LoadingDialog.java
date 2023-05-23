package com.example.friendverse.DialogLoadingBar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.friendverse.R;

public class LoadingDialog {
    Context context;
    Dialog dialog;
    public LoadingDialog(Context context){
        this.context = context;
    }

    public void showDialog(){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.create();
        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }
}
