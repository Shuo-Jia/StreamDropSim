package org.bjtu.iplab.js.receiver;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.TreeMap;

public class StreamRecvServer {
    private Map<Integer, ByteArrayModule> recvNaluMap;
    private RecvSocket recvSocket;
    private ReceverPacket receverPacket;
    private FileChannel fileChannel;

    public StreamRecvServer(String fileUrl, int port) throws IOException {
        this.recvNaluMap = new TreeMap<>();
        this.recvSocket = new RecvSocket(port);
        this.fileChannel = new FileOutputStream(new File(fileUrl)).getChannel();
    }

    public void recvAndSaveSimple() throws IOException {

        int seqNum = 0;
        while (true) {
            ByteBuffer recvByteBuffer = recvSocket.recvByteBuffer();
            System.out.println("recv:" + (seqNum++) + " size=" + recvByteBuffer.limit());
            if (recvByteBuffer.limit() == 1) {
                break;
            }
            fileChannel.write(recvByteBuffer);
        }
    }

    public void recvAndSave() throws IOException {
        do {
            byte[] recv_packet_info_bytes = recvSocket.recvPacket();
            receverPacket = new ReceverPacket(recv_packet_info_bytes);
            System.out.println(receverPacket.toString());
            int gop_num = receverPacket.gop_num;
            int packet_num = receverPacket.packet_num;
            ByteArrayModule sps_pps_nalu_bytes = transitToModule(receverPacket.sps_pps_nalu_bytes);
            ByteArrayModule general_nalu_bytes = transitToModule(receverPacket.general_nalu_bytes);
            recvNaluMap.put(gop_num, sps_pps_nalu_bytes);
            if (recvNaluMap.containsKey(packet_num)) {
                byte[] nalue = recvNaluMap.get(packet_num).bytes;
                System.out.println("*****************:" + nalue.length + " exited is drop? " + nalue[nalue.length -1]+" this is drop? " + general_nalu_bytes.bytes[general_nalu_bytes.bytes.length - 1]);
                if (nalue.length == 1 | nalue[nalue.length -1] != 0) {
                    continue;
                }
            }
            recvNaluMap.put(packet_num, general_nalu_bytes);
        } while (receverPacket.gop_num != 10000);

        for (Integer key : recvNaluMap.keySet()) {
            if (key != 10000) {
                fileChannel.write(ByteBuffer.wrap(recvNaluMap.get(key).bytes));
            }
        }
    }

    private ByteArrayModule transitToModule(byte[] bytes) {
        return new ByteArrayModule(bytes);
    }

    class ByteArrayModule {
        public byte[] bytes;

        public ByteArrayModule(byte[] bytes) {
            this.bytes = bytes;
        }
    }


    public static void main(String[] args) throws IOException {
        if (args[0].equals("simple")) {
            System.out.println("you are run simple receiver");
            StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\RaceHorses_832x480_30_1024_simple.h264", 4321);
            streamRecvServer.recvAndSaveSimple();
        } else if (args[0].equals("gop")) {
            System.out.println("you are run gop receiver");
            //StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\RaceHorses_832x480_30_1024_gop.h264", 4321);
            //StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\MOBILE_352x288_30_avc_384_gop.h264", 4321);
            //StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\CITY_352x288_30_avc_512_gop.h264", 4321);
            StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\BUS_352x288_30_avc_512_gop.h264", 4321);
            streamRecvServer.recvAndSave();
        } else if (args[0].equals("uep")) {
            System.out.println("you are run uep receiver");
            //StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\RaceHorses_832x480_30_1024_uep.h264", 4321);
            //StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\CITY_352x288_30_avc_512_uep.h264", 4321);
            StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\BUS_352x288_30_avc_512_uep.h264", 4321);
            //StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\MOBILE_352x288_30_avc_384_uep.h264", 4321);
            streamRecvServer.recvAndSave();
        } else if (args[0].equals("nonalu")) {
            System.out.println("you are run nonalue receiver");
            StreamRecvServer streamRecvServer = new StreamRecvServer("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\output\\RaceHorses_832x480_30_1024_nanalue.h264", 4321);
            streamRecvServer.recvAndSaveSimple();
        }
        System.out.println("OK");
    }
}
