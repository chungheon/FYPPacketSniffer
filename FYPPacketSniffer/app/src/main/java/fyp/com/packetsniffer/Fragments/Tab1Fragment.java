package fyp.com.packetsniffer.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fyp.com.packetsniffer.R;

public class Tab1Fragment extends Fragment {

    private static final int VPN_REQUEST_CODE = 0x0F;

    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_one, container, false);
        return view;
    }

}