package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class RecyclerViewPageAdapter extends RecyclerView.Adapter<RecyclerViewPageAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<String> pages;
    public RecyclerViewPageAdapter (Context context, ArrayList<String> pages){
        this.mContext = context;
        this.pages = pages;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_packet_capture_page, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.pageText.setText(pages.get(i));
        int colorId1 = mContext.getResources().getColor(R.color.colorPrimary);
        int colorId2 = mContext.getResources().getColor(R.color.colorPrimaryDark);
        if(i % 2 == 0){
            viewHolder.pageLayout.setBackgroundColor(colorId1);
        }else{
            viewHolder.pageLayout.setBackgroundColor(colorId2);
        }
        /*TextView tv = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 100);
        tv.setLayoutParams(layoutParams);
        tv.setText(pages.get(i));
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(Color.TRANSPARENT);

        Bitmap testB;

        testB = Bitmap.createBitmap(80, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(testB);
        tv.layout(0, 0, 80, 100);
        tv.draw(c);

        viewHolder.pageText.setLayoutParams(layoutParams);
        viewHolder.pageText.setBackgroundColor(Color.GRAY);
        viewHolder.pageText.setImageBitmap(testB);
        viewHolder.pageText.setMaxHeight(80);
        viewHolder.pageText.setMaxWidth(80);*/
    }

    public void bindList(ArrayList<String> data){
        this.pages = data;
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView pageText;
        public LinearLayout pageLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.pageText = (TextView) itemView.findViewById(R.id.capture_content);
            this.pageLayout = (LinearLayout) itemView.findViewById(R.id.page_layout);
        }
    }
}
