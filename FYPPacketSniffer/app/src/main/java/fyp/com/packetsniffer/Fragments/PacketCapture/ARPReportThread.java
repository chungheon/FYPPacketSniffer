package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ARPReportThread implements Runnable {
    public Thread thread;
    private ArrayList<byte[]> data;
    private AnalysisReportFragment mFragment;
    private ArrayList<String> requests;
    private ArrayList<String> replies;
    private String occurance = "";
    private ArrayList<String> diffRepArray;
    private ArrayList<String> sameMacs;
    private ArrayList<String> suspiciousIP;
    private ArrayList<String> suspiciousMAC;
    private boolean exit = false;
    private long numOfPackets;

    public ARPReportThread(AnalysisReportFragment fragment, ArrayList<byte[]> data){
        this.data = data;
        this.mFragment = fragment;
        this.numOfPackets = numOfPackets;
        thread = new Thread(this);
        thread.start();
    }

    public void run(){
        analyseARP();
    }

    private void analyseARP(){
        requests = new ArrayList<>();
        replies = new ArrayList<>();
        suspiciousIP  = new ArrayList<>();
        suspiciousMAC = new ArrayList<>();
        sameMacs = new ArrayList<>();
        diffRepArray = new ArrayList<>();
        String request = "";
        String reply = "";
        long reqCount = 0;
        long replyCount = 0;
        for(int j = 0; j < data.size(); j++) {
            String[] dataInfo = new String(data.get(j)).split("\\|");
            for (String s : dataInfo) {
                String line = s.replace("\n", "");
                if (line.matches(".*\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")) {
                    if (line.matches(".*ARP.*who-has.*")) {
                        request += "\n" + line;
                        reqCount++;
                        if (reqCount % 100 == 0) {
                            requests.add(request);
                            request = "";
                        }
                    } else if (line.matches(".*ARP.*Reply.*")) {
                        reply += "\n" + line;
                        replyCount++;
                        if (replyCount % 100 == 0) {
                            replies.add(reply);
                            reply = "";
                        }
                    }
                }
                if(this.exit){
                    return;
                }
            }
        }
        if(replyCount % 100 != 0){
            requests.add(request);
        }

        if(reqCount % 100 != 0){
            replies.add(reply);
        }

        String analysis = "";
        analysis += "ARP REQUEST: " + reqCount + "\n";
        analysis += "ARP REPLY: " + replyCount + "\n";
        analyseARPSpoof(requests, replies);
        analysis += occurance + "\n\n";

        if(sameMacs.isEmpty()){
            analysis += "No IP Addresses with same MACs\n\n";

        }else{
            analysis += "MAC address with mutiple IP Addresses"
                    + "\n-----------------";
            int countDiff = 0;
            int counter = 0;
            for(String diff: sameMacs) {
                if (countDiff > 50 || analysis.length() > 3000) {
                    analysis += "\n\n" + diff;
                    mFragment.printAnalysis(analysis);
                    analysis = "";
                    countDiff = 0;
                }else if(counter == 0){
                    analysis += "\n" + diff;
                    countDiff++;
                }else{
                    analysis += "\n\n" + diff;
                    countDiff++;
                }
                counter++;
            }
            if(this.exit){
                return;
            }
        }
        analysis += "\n\n";

        if(diffRepArray.isEmpty()){
            analysis += "No suspicious ARP replies\n\n";

            mFragment.printAnalysis(analysis);
        }else{
            analysis += "Multiple MAC addresses reply to IP Address Request"
                    + "\n-----------------";
            int countDiff = 0;
            int counter = 0;
            for(String diff: diffRepArray) {
                if (countDiff > 50 || analysis.length() > 3000) {
                    analysis += "\n\n" + diff;
                    mFragment.printAnalysis(analysis);
                    analysis = "";
                    countDiff = 0;
                }else if(counter == 0){
                    analysis += "\n" + diff;
                    countDiff++;
                }else{
                    analysis += "\n\n" + diff;
                    countDiff++;
                }

                if (counter == diffRepArray.size() - 1 && countDiff > 0) {
                    mFragment.printAnalysis(analysis);
                }
                if(this.exit){
                    return;
                }
                counter++;
            }
        }
        if(!suspiciousIP.isEmpty()){
            for(String sus: suspiciousIP){
                String header = "Suspicious ARP Replies with different MAC addresses\n\n";
                if(sus.length() > 8990){
                    String temp = header;
                    int count = 0;
                    String[] suspiciousStr = sus.split("\n");
                    for(String s: suspiciousStr){
                        temp += s + "\n";
                        if(temp.length() > 8000){
                            mFragment.printAnalysis(temp);
                            temp = "";
                            count = 0;
                        }
                        count++;
                    }
                    if(count > 0){
                        mFragment.printAnalysis(temp);
                    }
                }else{
                    mFragment.printAnalysis(header + sus);
                }
                if(this.exit){
                    return;
                }
            }
        }

        if(!suspiciousMAC.isEmpty()){
            for(String sus: suspiciousMAC){
                String header = "Suspicious ARP Replies with same MAC addresses\n\n";
                if(sus.length() > 8990){
                    String temp = header;
                    int count = 0;
                    String[] suspiciousStr = sus.split("\n");
                    for(String s: suspiciousStr){
                        temp += s + "\n";
                        if(temp.length() > 8000){
                            mFragment.printAnalysis(temp);
                            temp = "";
                            count = 0;
                        }
                        count++;
                    }
                    if(count > 0){
                        mFragment.printAnalysis(temp);
                    }
                }else{
                    mFragment.printAnalysis(header + sus);
                }
                if(this.exit){
                    return;
                }
            }
        }
    }

    private void analyseARPSpoof(ArrayList<String> requests,ArrayList<String> replies){
        HashMap<String, Pair<Long, String>> replyStore = new HashMap<>();
        for(String repliesStr: replies){
            String[] repliesArray = repliesStr.split("\n");
            for(String reply: repliesArray){
                String[] replyInfo = reply.split("Reply");
                if(replyInfo.length >= 2){
                    String[] temp = replyInfo[1].substring(1).split(" ");
                    String rep = "";
                    if(temp.length >= 3){
                        rep = temp[0];
                        if(replyStore.containsKey(rep)){
                            String lines = replyStore.get(rep).second + "\n" + reply;
                            long occur = replyStore.get(rep).first + new Long(1);
                            Pair newValue = new Pair(occur, lines);
                            replyStore.put(rep, newValue);
                        }else{
                            Pair newValue = new Pair(new Long(1), reply);
                            replyStore.put(rep, newValue);
                        }
                    }
                    if(this.exit){
                        return;
                    }
                }
            }
        }

        analyseReqRep(replyStore, requests);
    }

    private String findDifferentReply(String allReplies, HashMap<String, Pair<Long, String>> MACs, String ipAddr){
        String diffRep = "";
        HashMap<String, Long> replies = new HashMap<>();
        String[] allRep = allReplies.split("\n");
        for(String reply: allRep){
            String[] replyInfo = reply.split("Reply");
            if(replyInfo.length >= 2){
                String[] temp = replyInfo[1].substring(1).split(" ");
                String rep = "";
                if(temp.length >= 3){
                    rep = temp[2];
                    if(!replies.containsKey(rep)){ ;
                        replies.put(rep, new Long(1));
                    }
                    if(MACs.containsKey(rep)){
                        boolean same = false;
                        Pair<Long,String> val = MACs.get(rep);
                        String[] allReply = val.second.split("\n");
                        for(String r: allReply){
                            if(r.matches(".*" + ipAddr + ".*")){
                                same = true;
                            }
                        }
                        if(!same){
                            Pair<Long, String> newVal = new Pair<>(val.first + new Long(1),
                                    val.second + "\n" + ipAddr);
                            MACs.put(rep, newVal);
                        }

                    }else{
                        Pair<Long, String> newVal = new Pair<>(new Long(1), ipAddr);
                        MACs.put(rep, newVal);
                    }
                }
            }
            if(this.exit){
                break;
            }
        }

        if(replies.keySet().size() > 1 && !exit){
            String header = "IP Address Request: " + ipAddr + "\n\n";
            this.suspiciousIP.add(header + allReplies);
            diffRep = "Reply for " + ipAddr + ":\n";
            for(String key: replies.keySet()){
                int occur = 0;
                for(String reply: allRep){
                    if(reply.matches(".*" + key + ".*")){
                        occur++;
                    }
                }
                diffRep += " MAC - " +  key + " : " + occur + "\n";
            }
        }

        return diffRep;
    }

    private void analyseReqRep( HashMap<String, Pair<Long, String>> replyStore, ArrayList<String> requests){
        int lastCounter = 1;
        HashMap<String, Pair<Long, String>> MACs = new HashMap<>();
        String diffRep = "";
        for(String reply: replyStore.keySet()){
            Pair<Long, String> value = replyStore.get(reply);
            long occur = replyStore.get(reply).first;

            diffRep  = findDifferentReply(value.second, MACs, reply);
            if(!diffRep.equals("")){
                diffRepArray.add(diffRep);
            }

            long count = 0;
            for(String request: requests){
                String[] reqIP = request.split("\n");
                for(String ipReq: reqIP){
                    if(ipReq.matches(".*Request who-has " + reply + ".*")){
                        count++;
                    }
                    if(this.exit){
                        return;
                    }
                }

            }
            if(lastCounter == replyStore.keySet().size()){
                occurance += reply + "- Replies: " + occur + "  Requests: " + count;
            }else{
                occurance += reply + "- Replies: " + occur + "  Requests: " + count + "\n";
                lastCounter++;
            }
            if(this.exit){
                return;
            }
        }
        findSameMacs(replyStore, MACs);
    }

    private void findSameMacs(HashMap<String, Pair<Long, String>> replyStore, HashMap<String, Pair<Long, String>> MACs){
        for(String key: MACs.keySet()){
            Pair<Long, String> value = MACs.get(key);
            String sus = "";
            if(value.first > new Long(1)){
                sus = "Reply with MAC Address: " + key + "\n";
                String header = "MAC " + key + " has multiple IP Addresses:\n";
                sameMacs.add(header + value.second);
                for(String reply: replyStore.keySet()){
                    String[] allRep = replyStore.get(reply).second.split("\n");
                    for(String r: allRep){
                        if(r.matches(".*" + key + ".*")){
                            sus += "\n" + r;
                        }
                    }
                }
                suspiciousMAC.add(sus);
            }
            if(this.exit){
                return;
            }
        }
    }

    public void stopRun(){
        this.exit = true;
    }
}
