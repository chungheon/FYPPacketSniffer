package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import fyp.com.packetsniffer.R;

public class PacketCaptureFragment extends Fragment {

    private static final int VPN_REQUEST_CODE = 0x0F;

    private View view;

    private TextView output;

    private Button sniffBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_capture, container, false);
        this.output = (TextView) view.findViewById(R.id.outputText);
        this.sniffBtn = (Button) view.findViewById(R.id.sniffBtn);
        sniffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String args = "su:-c:tcpdump -tttt -vvv -e -c 5";
                String[] cmd = args.split(":");
                CmdExec test = new CmdExec();
                String result = test.executeCMD(cmd);
                output.setText(result);
            }
        });

        /*String args = "su:-c:tcpdump -tttt -vvv -e -c 5";
        String[] cmd = args.split(":");
        CmdExec test = new CmdExec();
        String result = test.executeCMD(cmd);
        this.output.setText(result);*/
        return view;
    }

}