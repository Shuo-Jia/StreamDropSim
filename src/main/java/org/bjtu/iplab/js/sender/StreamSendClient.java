package org.bjtu.iplab.js.sender;

import org.bjtu.iplab.js.StreamInfo.NaluInfo;

import java.io.IOException;

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

    public StreamSendClient(String fileUrl, String ip, int port) throws IOException {
        this.fileOffset = 0;
        this.gop_num = 0;
        this.packet_num = 0;
        this.sendSocket = new SendSocket(ip, port);
        this.overDataBuf = new byte[1400];

        this.naluParseFile = new NaluParseFile(fileUrl);
    }

    public void packetAndSendSimple() throws IOException {
        while (naluParseFile.isAvaliable()) {
            naluInfo = naluParseFile.getNextNalu(fileOffset);
            fileOffset = naluInfo.offset + naluInfo.length;

            if (naluInfo.length < 1400) {
                sendSocket.sendBytes(naluInfo.payload);
            } else {
                int remaining = naluInfo.length;
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
        while (naluParseFile.isAvaliable()) {
            naluInfo = naluParseFile.getNextNalu(fileOffset);
            fileOffset = naluInfo.offset + naluInfo.length;
            System.out.println(naluInfo.toString());

            if (naluInfo.nal_unit_type == 7) {
                gop_num = (++packet_num);
                naluInfo_sps = naluInfo;
                naluInfo = naluParseFile.getNextNalu(fileOffset);
                fileOffset = naluInfo.offset + naluInfo.length;
                naluInfo_pps = naluInfo;
                System.out.println(naluInfo.toString());
                System.out.println("===========================================================================================================================================================");
            } else {
                if (naluInfo.length < 1400) {
                    packet_num++;
                    SenderPacket senderPacket = new SenderPacket(gop_num, packet_num, naluInfo_sps.payload, naluInfo_pps.payload, naluInfo.payload);
                    sendSocket.sendPacket(senderPacket);
                } else {
                    int remaining = naluInfo.length;
                    int nalu_offset = 0;
                    for (; remaining > 1400; remaining = remaining - 1400) {
                        packet_num++;
                        System.arraycopy(naluInfo.payload, nalu_offset, overDataBuf, 0, 1400);
                        nalu_offset = nalu_offset + 1400;
                        SenderPacket senderPacket = new SenderPacket(gop_num, packet_num, naluInfo_sps.payload, naluInfo_pps.payload, overDataBuf);
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
                    System.arraycopy(naluInfo.payload, nalu_offset, remainingBuf, 0, remaining);
                    SenderPacket senderPacket = new SenderPacket(gop_num, packet_num, naluInfo_sps.payload, naluInfo_pps.payload, remainingBuf);
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
        SenderPacket senderPacket = new SenderPacket(10000, 10000, naluInfo_sps.payload, naluInfo_pps.payload, overDataBuf);
        sendSocket.sendPacket(senderPacket);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        StreamSendClient streamSendClient = new StreamSendClient("G:\\WORK_FILE\\VC\\StreamDropsimulation\\StreamSendClient\\Resource\\RaceHorses_832x480_30_2048.h264", "120.77.41.168", 4321);
        if (args[0].equals("simple")) {
            System.out.println("you are run simple sender");
            streamSendClient.packetAndSendSimple();
        } else if (args[0].equals("gop")) {
            streamSendClient.packetAndSendGOP(false);
        } else if (args[0].equals("uep")) {
            streamSendClient.packetAndSendGOP(true);
        }
        System.out.println("OK!");
    }
}
