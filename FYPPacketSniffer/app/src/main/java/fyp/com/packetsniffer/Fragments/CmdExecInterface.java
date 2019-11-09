package fyp.com.packetsniffer.Fragments;

import java.util.ArrayList;

public interface CmdExecInterface {
    public void printResult(final ArrayList<String> result, final long numOfPackets);

    public void printToast(final String message);

    public void cmdDone();
}
