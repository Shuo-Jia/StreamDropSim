package org.bjtu.iplab.js.sender;

import org.bjtu.iplab.js.StreamInfo.NaluInfo;

import java.io.IOException;
import java.io.RandomAccessFile;

public class StreamSendClient {
    private int fileOffset;
    private int gop_num;
    private int packet_num;

    private NaluInfo naluInfo;
    private NaluInfo naluInfo_sps;
    private NaluInfo naluInfo_pps;

    private NaluParseFile naluParseFile;
    private SendSocket sendSocket;


    private byte[] overDataBuf;
    private byte[] overDataStartBuf;


    public StreamSendClient(String fileUrl, String ip, int port) throws IOException {
        this.fileOffset = 0;
        this.gop_num = 0;
        this.packet_num = 0;
        this.sendSocket = new SendSocket(ip, port);

        this.naluParseFile = new NaluParseFile(fileUrl);
    }


    RandomAccessFile fileInputRandomStream = new RandomAccessFile("test.h264", "rw");

    public void packetAndSendNoNalu() throws IOException, InterruptedException {
        int index = 0;
        while (naluParseFile.isAvaliable()) {
            index++;
            byte[] res = naluParseFile.getMTUpayload();
            sendSocket.sendBytes(res);
        }
        System.out.println("finish!!!!!!!");
        sendSocket.sendBytes(new byte[1]);
    }

    public void packetAndSendSimple() throws IOException, InterruptedException {
        while (naluParseFile.isAvaliable()) {
            naluInfo = naluParseFile.getNextNalu(fileOffset);
            fileOffset = naluInfo.offset + naluInfo.length_with_start;

            if (naluInfo.length_with_start < 1400) {
                sendSocket.sendBytes(naluInfo.payload);
            } else {
                int remaining = naluInfo.length_with_start;
                int nalu_offset = 0;
                for (; remaining > 1400; remaining = remaining - 1400) {
                    System.arraycopy(naluInfo.payload, nalu_offset, overDataBuf, 0, 1400);
                    sendSocket.sendBytes(overDataBuf);
                    nalu_offset = nalu_offset + 1400;
                }
                byte[] remainingBuf = new byte[remaining];
                System.arraycopy(naluInfo.payload, nalu_offset, remainingBuf, 0, remaining);
                sendSocket.sendBytes(remainingBuf);
            }
        }
        sendSocket.sendBytes(new byte[1]);
    }

    public void packetAndSendGOP(boolean uep) throws IOException, InterruptedException {
        SenderPacket senderPacket = null;
        while (naluParseFile.isAvaliable()) {
            naluInfo = naluParseFile.getNextNalu(fileOffset);
            fileOffset = naluInfo.offset + naluInfo.length_with_start;
            System.out.println(naluInfo.toString());

            if (naluInfo.nal_unit_type == 7) {
                gop_num = (++packet_num);
                naluInfo_sps = naluInfo;
                naluInfo = naluParseFile.getNextNalu(fileOffset);
                fileOffset = naluInfo.offset + naluInfo.length_with_start;
                naluInfo_pps = naluInfo;
                System.out.println(naluInfo.toString());
                System.out.println("===========================================================================================================================================================");
            } else {
                if (naluInfo.length_with_start < 1400) {
                    packet_num++;
                    senderPacket = new SenderPacket(gop_num, packet_num, naluInfo_sps.allDataload, naluInfo_pps.allDataload, naluInfo.startload, naluInfo.payload);
                    sendSocket.sendPacket(senderPacket);
                } else {
                    int remaining = naluInfo.length_with_start;
                    int nalu_offset = 0;
                    for (; remaining > 1400; remaining = remaining - 1400) {
                        packet_num++;
                        if (nalu_offset == 0) {
                            overDataStartBuf = new byte[naluInfo.startLen];
                            overDataBuf = new byte[1400 - naluInfo.startLen];
                            System.arraycopy(naluInfo.startload, 0, overDataStartBuf, 0, naluInfo.startLen);
                            System.arraycopy(naluInfo.payload, 0, overDataBuf, 0, 1400 - naluInfo.startLen);
                            senderPacket = new SenderPacket(gop_num, packet_num, naluInfo_sps.allDataload, naluInfo_pps.allDataload, overDataStartBuf, overDataBuf);
                        } else {
                            overDataBuf = new byte[1400];
                            System.arraycopy(naluInfo.payload, nalu_offset - naluInfo.startLen, overDataBuf, 0, 1400);
                            senderPacket = new SenderPacket(gop_num, packet_num, naluInfo_sps.allDataload, naluInfo_pps.allDataload, new byte[0], overDataBuf);
                        }
                        nalu_offset = nalu_offset + 1400;
                        if (uep) {
                            if (naluInfo.nal_reference_idc == 96) {
                                System.out.print(">>>>>IDR:");
                                sendSocket.sendPacket(senderPacket);
                                Thread.sleep(10);
                                System.out.print(">>>>>IDR:");
                                sendSocket.sendPacket(senderPacket);
                                Thread.sleep(10);
                            } else if (naluInfo.nal_reference_idc == 64) {
                                System.out.print(">>>>>BR:");
                                sendSocket.sendPacket(senderPacket);
                                Thread.sleep(10);
                            }
                        }
                        sendSocket.sendPacket(senderPacket);
                    }
                    packet_num++;
                    byte[] remainingBuf = new byte[remaining];
                    System.arraycopy(naluInfo.payload, nalu_offset - naluInfo.startLen, remainingBuf, 0, remaining);
                    senderPacket = new SenderPacket(gop_num, packet_num, naluInfo_sps.allDataload, naluInfo_pps.allDataload, new byte[0], remainingBuf);
                    if (uep) {
                        if (naluInfo.nal_reference_idc == 96) {
                            System.out.print(">>>>>IDR:");
                            sendSocket.sendPacket(senderPacket);
                            Thread.sleep(10);
                            System.out.print(">>>>>IDR:");
                            sendSocket.sendPacket(senderPacket);
                            Thread.sleep(10);
                        } else if (naluInfo.nal_reference_idc == 64) {
                            System.out.print(">>>>>BR:");
                            sendSocket.sendPacket(senderPacket);
                            Thread.sleep(10);
                        }
                    }
                    sendSocket.sendPacket(senderPacket);
                }
            }
        }
        senderPacket = new SenderPacket(10000, 10000, naluInfo_sps.allDataload, naluInfo_pps.allDataload, new byte[0], overDataBuf);
        sendSocket.sendPacket(senderPacket);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        StreamSendClient streamSendClient = new StreamSendClient("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\resoures\\BUS_352x288_30_avc_512.h264", "127.0.0.1", 4321);
        //StreamSendClient streamSendClient = new StreamSendClient("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\resoures\\CITY_352x288_30_avc_512.h264", "127.0.0.1", 4321);
        //StreamSendClient streamSendClient = new StreamSendClient("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\resoures\\RaceHorses_832x480_30_1024.h264", "127.0.0.1", 4321);
        //StreamSendClient streamSendClient = new StreamSendClient("G:\\WORK_FILE\\RTCLab\\SteamDropSim\\resoures\\MOBILE_352x288_30_avc_384.h264", "127.0.0.1", 4321);

        if (args[0].equals("simple")) {
            System.out.println("you are run simple sender");
            streamSendClient.packetAndSendSimple();
        } else if (args[0].equals("gop")) {
            streamSendClient.packetAndSendGOP(false);
        } else if (args[0].equals("uep")) {
            streamSendClient.packetAndSendGOP(true);
        } else if (args[0].equals("nonalu")) {
            streamSendClient.packetAndSendNoNalu();
        }
        System.out.println("OK!");
    }
}
