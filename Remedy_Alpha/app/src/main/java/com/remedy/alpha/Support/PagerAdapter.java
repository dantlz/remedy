package com.remedy.alpha.Support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.remedy.alpha.R;

public class PagerAdapter extends android.support.v4.view.PagerAdapter {
    Context context;
    LayoutInflater inflater;

    public int[] backIMGs = {
            R.drawable.prob_desc_bg,
            R.drawable.contact_bg,
            R.drawable.contact_info_bg,
            R.drawable.chat_queue_bg
    };

    public PagerAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = null;
        //If page is problem description
        if(position == 0){
            view = inflater.inflate(R.layout.pager_prob_desc, container, false);
        } else if(position == 1){

        } else if(position == 2){

        } else if(position == 3){

        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
