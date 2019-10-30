package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fyp.com.packetsniffer.R;

public class NetworkUtilsFragment extends Fragment {

    private View view;
    private RecyclerView networkUtils;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_network_utils, container, false);
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.networkUtils = (RecyclerView) view.findViewById(R.id.network_util_recyclerview);

    }

    private void initListener(){

    }
}
