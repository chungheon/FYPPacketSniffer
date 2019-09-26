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

    private final String TAG = "PCapFragment";
    private final int PCAP_NORMAL_STREAM = 1;
    private final int PCAP_ERROR_STREAM = 2;
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
                String getVersion = runCmd("su:-c:tcpdump --version", PCAP_ERROR_STREAM);
                String testPacket = runCmd("su:-c:tcpdump -tttt -vvv -e -c 5", PCAP_NORMAL_STREAM);
                output.setText(getVersion + "\n\n" + testPacket);

            }
        });
        return view;
    }

    private String runCmd(String args, int mode){
        String[] cmd = args.split(":");
        CmdExec exec = new CmdExec();
        return exec.executeCMD(cmd, mode);
    }
}