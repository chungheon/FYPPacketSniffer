package fyp.com.packetsniffer.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fyp.com.packetsniffer.R;

public class Tab1Fragment extends Fragment {
    int num = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        TextView tx = view.findViewById(R.id.fragText);
        num = this.getArguments().getInt("num");
        tx.setText(num + " fragment");
        return view;
    }
}