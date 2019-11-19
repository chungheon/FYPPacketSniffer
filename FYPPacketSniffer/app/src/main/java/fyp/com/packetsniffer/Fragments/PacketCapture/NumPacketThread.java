package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;
import android.util.Pair;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class NumPacketThread implements Runnable{
    public Thread thread;
    AnalysisReportFragment mFragment;
    private ArrayList<byte[]> data;
    private ArrayList<byte[]> dataSend;
    private boolean exit = false;

    public NumPacketThread(AnalysisReportFragment fragment, ArrayList<byte[]> data){
        this.data = data;
        this.mFragment = fragment;
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        gatherIPAddrStats();
    }
    private void gatherIPAddrStats(){
        HashMap<String, Double> numSend = new HashMap<>();
        HashMap<String, Double> numRecv = new HashMap<>();
        HashMap<String, Long> numSync = new HashMap<>();
        HashMap<String, Long> numRep = new HashMap<>();
        HashMap<String, Long> numAck = new HashMap<>();
        String ipSend = "";
        String ipRecv = "";
        for(byte[] data: this.data){
            String[] allPacket = new String(data).split("\n");
            for(String packet: allPacket){
                String[] dataInfo = packet.split("\\|");
                if(dataInfo.length >= 2){
                    String process = dataInfo[1];
                    String[] packetInfo = process.split(":");
                    String[] processInfo = packetInfo[0].split(">");
                    if(processInfo.length >= 2){
                        String[] temp = processInfo[0].split("\\.");
                        ipSend = temp[0] + "." + temp[1] + "." + temp[2] + "." + temp[3];
                        ipSend = ipSend.replace(" ", "");
                        temp = processInfo[1].split("\\.");
                        ipRecv = temp[0] + "." + temp[1] + "." + temp[2] + "." + temp[3];
                        ipRecv = ipRecv.replace(" ", "");
                        if(ipSend.matches("\\d*\\.\\d*\\.\\d*\\.\\d*")){
                            if(numSend.containsKey(ipSend)){
                                numSend.put(ipSend, numSend.get(ipSend) + new Double(1));
                            }else{
                                numSend.put(ipSend, new Double(1));
                            }
                        }
                        if(ipRecv.matches("\\d*\\.\\d*\\.\\d*\\.\\d*")){
                            if(numRecv.containsKey(ipRecv)){
                                numRecv.put(ipRecv, numRecv.get(ipRecv) + new Double(1));
                            }else{
                                numRecv.put(ipRecv, new Double(1));
                            }
                        }
                    }

                    if(packet.matches(".*\\[S\\.?\\].*")){
                        Log.d("OUT", packet);
                        String[] temp = packetInfo[0].split("\\.");
                        if(temp.length >= 8){
                            String sender = temp[0].replace(" ", "") + "." + temp[1] + "." + temp[2] + "." + temp[3];
                            String first = temp[4].replace(" ", "").split(">")[1];
                            String recv = first + "." + temp[5] + "." + temp[6] + "." + temp[7];
                            String sync = sender + " > " + recv;
                            if(numSync.containsKey(sync)){
                                numSync.put(sync, numSync.get(sync) + Long.valueOf(1));
                            }else{
                                numSync.put(sync, Long.valueOf(1));
                            }
                        }


                    }else if(packet.matches(".*\\[A\\.?\\].*")){
                        String[] temp = packetInfo[0].split("\\.");
                        if(temp.length >= 8){
                            String sender = temp[0].replace(" ", "") + "." + temp[1] + "." + temp[2] + "." + temp[3];
                            String first = temp[4].replace(" ", "").split(">")[1];
                            String recv = first + "." + temp[5] + "." + temp[6] + "." + temp[7];
                            String ack = sender + " > " + recv;
                            if(numAck.containsKey(ack)){
                                numAck.put(ack, numAck.get(ack) + Long.valueOf(1));
                            }else{
                                numAck.put(ack, Long.valueOf(1));
                            }
                        }

                    }else if(packet.matches(".*\\[S\\.?,.?A\\.?\\].*")){
                        String[] temp = packetInfo[0].split("\\.");
                        if(temp.length >= 8){
                            String sender = temp[0].replace(" ", "") + "." + temp[1] + "." + temp[2] + "." + temp[3];
                            String first = temp[4].replace(" ", "").split(">")[1];
                            String recv = first + "." + temp[5] + "." + temp[6] + "." + temp[7];
                            String rep = recv + " > " + sender;
                            if(numRep.containsKey(rep)){
                                numRep.put(rep, numRep.get(rep) + Long.valueOf(1));
                            }else{
                                numRep.put(rep, Long.valueOf(1));
                            }
                        }
                    }
                }
                if(this.exit){
                    return;
                }
            }
            List<Pair<String, Double>> update = updateInfoDouble(numSend);
            mFragment.printNumSend(update);
            update = updateInfoDouble(numRecv);
            mFragment.printNumRecv(update);
        }
        numSend.clear();
        numRecv.clear();

        String analysis = "";
        List<Pair<String, Long>> update;
        if(numSync.keySet().isEmpty()){
            analysis += "No only sync packets\n";
        }else{
            analysis += "\nSyn Sent: \n";
            update = updateInfoLong(numSync);
            for(Pair<String, Long> data: update){
                analysis += data.first + ": " + data.second.toString() + "\n";
            }
        }

        if(numRep.keySet().isEmpty()){
            analysis += "\nNo sync and ack packets\n";
        }else{
            analysis += "\nSyn-Ack Sent: \n";
            update = updateInfoLong(numRep);
            for(Pair<String, Long> data: update) {
                String[] temp = data.first.split("\\.");
                if (temp.length >= 8) {
                    String sender = temp[0].replace(" ", "") + "." + temp[1] + "." + temp[2] + "." + temp[3];
                    String first = temp[4].replace(" ", "").split(">")[1];
                    String recv = first + "." + temp[5] + "." + temp[6] + "." + temp[7];
                    String rep = recv + " > " + sender;
                    analysis += rep + ": " + data.second.toString() + "\n";
                }
            }
        }

        if(numAck.keySet().isEmpty()){
            analysis += "\nNo ack packets";
        }else{
            analysis += "\nAck Sent: \n";
            update = updateInfoLong(numAck);
            for(Pair<String, Long> data: update){
                analysis += data.first + ": " + data.second.toString() + "\n";
            }
        }

        mFragment.printSynAck(analysis);

        /*if(!numAck.keySet().isEmpty() || !numSync.keySet().isEmpty()){
            analysis += "\n\n";
            HashMap<String, Long> stats = analyseSynAck(numSync, numAck, numRep);
            update = updateInfoLong(stats);

            for(Pair<String, Long> data: update){
                analysis += "\n" + data.first + ":\n\tSYN: "
                        + numSync.get(data.first);
                if(numRep.containsKey(data.first) && numAck.containsKey(data.first)){
                    analysis += " SYN-ACK: " + numRep.get(data.first);
                    analysis += " ACK: " + numAck.get(data.first);
                }
                if(data.second > 0 && data.second > 1000){
                    analysis += "\nOverhead: " + data.second + " Sync (Suspicious)";
                }else if(data.second > 0 && data.second > 500){
                    analysis += "\nOverhead: " + data.second + " Sync (Warn)";
                }else if(data.second > 0){
                    analysis += "\nOverhead: " + data.second;
                }
            }

            mFragment.printSynAckUpdate(analysis);
        }*/


    }
    private List<Pair<String, Double>> updateInfoDouble(HashMap<String, Double> numPackets){
        List<Pair<String, Double>> dataArr = new ArrayList<>();
        for(String key: numPackets.keySet()){
            Pair<String, Double> pair = new Pair<>(key,numPackets.get(key));
            dataArr.add(pair);
        }
        Collections.sort(dataArr, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return (int) (o2.second - o1.second);
            }
        });
        return dataArr;
    }

    private List<Pair<String, Long>> updateInfoLong(HashMap<String, Long> numPackets){
        List<Pair<String, Long>> dataArr = new ArrayList<>();
        for(String key: numPackets.keySet()){
            Pair<String, Long> pair = new Pair<>(key,numPackets.get(key));
            dataArr.add(pair);
        }
        Collections.sort(dataArr, new Comparator<Pair<String, Long>>() {
            @Override
            public int compare(Pair<String, Long> o1, Pair<String, Long> o2) {
                return (int) (o2.second - o1.second);
            }
        });
        return dataArr;
    }

    private HashMap<String, Long> analyseSynAck(HashMap<String, Long> sync, HashMap<String, Long> ack, HashMap<String, Long> rep){
        HashMap<String, Long> stats = new HashMap<>();
        for(String key: sync.keySet()){
            long count = Long.valueOf(0);
            if(rep.containsKey(key) && ack.containsKey(key)){
                stats.put(key, sync.get(key) - rep.get(key) + (rep.get(key) - ack.get(key)));
            }else if(rep.containsKey(key)){
                stats.put(key, sync.get(key));
            }else{
                stats.put(key, sync.get(key));
            }

            if(exit){
                return stats;
            }
        }
        return stats;
    }

    public void stopRun(){
        this.exit = true;
    }
}
