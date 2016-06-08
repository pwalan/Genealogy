package com.github.pwalan.genealogy.utils;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

/**
 * ListView显示Bitmap图片需要让Adapter设置的类
 */
public class ListViewBinder implements SimpleAdapter.ViewBinder{
    @Override
    public boolean setViewValue(View view, Object data,
                                String textRepresentation) {
        // TODO Auto-generated method stub
        if((view instanceof ImageView) && (data instanceof Bitmap)) {
            ImageView imageView = (ImageView) view;
            Bitmap bmp = (Bitmap) data;
            imageView.setImageBitmap(bmp);
            return true;
        }
        return false;
    }
}
