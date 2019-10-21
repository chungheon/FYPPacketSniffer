package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fyp.com.packetsniffer.R;

public class SelectPacketFragment extends Fragment{

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_select_packet, container, false);
        initView();
        return view;
    }

    public void initView(){

    }
}
