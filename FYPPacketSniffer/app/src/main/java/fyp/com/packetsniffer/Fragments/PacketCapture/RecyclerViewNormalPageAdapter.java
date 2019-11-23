package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import fyp.com.packetsniffer.R;

public class RecyclerViewNormalPageAdapter extends RecyclerView.Adapter<RecyclerViewNormalPageAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<byte[]> pages;
    public RecyclerViewNormalPageAdapter (Context context, ArrayList<byte[]> pages){
        this.mContext = context;
        this.pages = pages;
    }

    public RecyclerViewNormalPageAdapter (Context context, String message){
        this.mContext = context;
        this.pages = new ArrayList<>();
        pages.add(message.getBytes());
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_packet_page, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.pageText.setText(new String(pages.get(i)));
        viewHolder.pageScroll.fullScroll(ScrollView.FOCUS_UP);
        int colorId1 = mContext.getResources().getColor(R.color.cyan);
        int colorId2 = mContext.getResources().getColor(R.color.cyanDark);
        if(i % 2 == 0){
            viewHolder.pageLayout.setBackgroundColor(colorId1);
        }else{
            viewHolder.pageLayout.setBackgroundColor(colorId2);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView pageText;
        public LinearLayout pageLayout;
        public ScrollView pageScroll;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.pageText = (TextView) itemView.findViewById(R.id.page_content);
            this.pageLayout = (LinearLayout) itemView.findViewById(R.id.page_layout);
            this.pageScroll = (ScrollView) itemView.findViewById(R.id.page_scroll);
            ViewCompat.setNestedScrollingEnabled(pageScroll, true);
        }
    }

    public void addPage(String page){
        pages.add(page.getBytes());
    }

    public void addPage(String page, int index) { pages.add(index, page.getBytes());}

    public void updatePage(String page, int index) {
        pages.remove(index);
        pages.add(index, page.getBytes());
    }

    public ArrayList<byte[]> getData(){
        return pages;
    }
}
