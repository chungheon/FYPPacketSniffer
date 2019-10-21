package fyp.com.packetsniffer.Fragments.PacketCapture;

import java.util.ArrayList;

public interface PacketCaptureInterface {
    public void printResult(final ArrayList<String> result, final long numOfPackets);

    public void printToast(final String message);
}
