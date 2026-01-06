package com.siti.mobile.Utils;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class ForensicHelper {

    private final TextView tvFp1;
    private final TextView tvFp2;
    private final TextView tvFp3;
    private final TextView tvFp4;
    private final String forensic;
    private final int delayFp;
    private final int delayShowIn;
    private final float transparency;
    private boolean stillShowing = true;

    public ForensicHelper(TextView tvFp1, TextView tvFp2, TextView tvFp3, TextView tvFp4,int delayFp, int delayShowIn ,String forensic, float transparency) {
        this.tvFp1 = tvFp1;
        this.tvFp2 = tvFp2;
        this.tvFp3 = tvFp3;
        this.tvFp4 = tvFp4;
        this.forensic = forensic;
        this.delayFp = delayFp;
        this.delayShowIn = delayShowIn;
        this.transparency = transparency;
    }

    public void start() {
        tvFp1.setText(forensic);
        tvFp2.setText(forensic);
        tvFp3.setText(forensic);
        tvFp4.setText(forensic);
        tvFp1.setAlpha(transparency);
        tvFp2.setAlpha(transparency);
        tvFp3.setAlpha(transparency);
        tvFp4.setAlpha(transparency);
        new Handler().postDelayed(runnableFp1, delayFp);
    }

    public void stop(){
        stillShowing = false;
        tvFp1.setVisibility(View.GONE);
        tvFp2.setVisibility(View.GONE);
        tvFp3.setVisibility(View.GONE);
        tvFp4.setVisibility(View.GONE);
    }


    private final Runnable runnableFp1 = new Runnable() {
        @Override
        public void run() {
            tvFp4.setVisibility(View.INVISIBLE);
            tvFp1.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvFp1.setVisibility(View.INVISIBLE);
                    if(stillShowing) {
                        new Handler().postDelayed(runnableFp2, delayFp);
                    }
                }
            }, delayShowIn);

        }
    };

    private final Runnable runnableFp2 = new Runnable() {
        @Override
        public void run() {
            tvFp1.setVisibility(View.INVISIBLE);
            tvFp2.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvFp2.setVisibility(View.INVISIBLE);
                    if(stillShowing) {
                        new Handler().postDelayed(runnableFp3, delayFp);
                    }
                }
            }, delayShowIn);

        }
    };

    private final Runnable runnableFp3 = new Runnable() {
        @Override
        public void run() {
            tvFp2.setVisibility(View.INVISIBLE);
            tvFp3.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvFp3.setVisibility(View.INVISIBLE);
                    if(stillShowing) {
                        new Handler().postDelayed(runnableFp4, delayFp);
                    }
                }
            }, delayShowIn);

        }
    };

    private final Runnable runnableFp4 = new Runnable() {
        @Override
        public void run() {
            tvFp3.setVisibility(View.INVISIBLE);
            tvFp4.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvFp4.setVisibility(View.INVISIBLE);
                    if(stillShowing) {
                        new Handler().postDelayed(runnableFp1, delayFp);
                    }
                }
            }, delayShowIn);

        }
    };

}

