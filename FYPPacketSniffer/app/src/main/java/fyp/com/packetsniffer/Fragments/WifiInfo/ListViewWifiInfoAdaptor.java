package fyp.com.packetsniffer.Fragments.WifiInfo;

import android.content.Context;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class ListViewWifiInfoAdaptor extends ArrayAdapter<Pair<String, String>> {
    private Context mContext;
    private int mResource;
    private ArrayList<Pair<String, String>> info;
    public ListViewWifiInfoAdaptor(Context context, int resource, ArrayList<Pair<String, String>> info) {
        super(context, resource, info);
        this.mContext = context;
        this.mResource = resource;
        this.info = info;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(mResource,parent, false);

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView value = (TextView) view.findViewById(R.id.value);
        ConstraintLayout layout = (ConstraintLayout) view.findViewById(R.id.info_bg);

        try{
            int bgColor = mContext.getResources().getColor(R.color.cyan);
            if(position % 2 == 0){
                bgColor = mContext.getResources().getColor(R.color.textBGColor);
            }else{
                bgColor = mContext.getResources().getColor(R.color.wifi_info_2);
            }

            layout.setBackgroundColor(bgColor);
        }catch(Resources.NotFoundException e){ }

        title.setText(info.get(position).first);
        value.setText(info.get(position).second);

        return view;
    }
}
