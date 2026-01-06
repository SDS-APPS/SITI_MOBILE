package com.siti.mobile.mvvm.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.siti.mobile.Model.advertisment.AdvertismentModel;
import com.siti.mobile.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import static com.siti.mobile.Utils.UtilKotlinKt.changeLocalToGlobalIfRequired;

import java.util.ArrayList;
import java.util.List;

public class SliderAdAdapter extends
        SliderViewAdapter<SliderAdAdapter.SliderAdapterVH> {

    private Context context;
    private List<AdvertismentModel> mSliderItems = new ArrayList<>();

    public SliderAdAdapter(Context context) {
        this.context = context;
    }

    public void renewItems(List<AdvertismentModel> sliderItems) {
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.mSliderItems.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(AdvertismentModel sliderItem) {
        this.mSliderItems.add(sliderItem);
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_ad_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        AdvertismentModel sliderItem = mSliderItems.get(position);
        Glide.with(viewHolder.itemView)
                .load(changeLocalToGlobalIfRequired(sliderItem.getUrl()))
                .fitCenter()
                .into(viewHolder.imgAd);

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imgAd;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imgAd = itemView.findViewById(R.id.imgAd);
            this.itemView = itemView;
        }
    }

}