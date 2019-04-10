package org.bjtu.iplab.js.sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;

public class SendSocket {

    private int sendNum = 0;
    private DatagramChannel datagramChannelSender;

    public SendSocket(String ip, int port) throws IOException {
        this.datagramChannelSender = DatagramChannel.open();
        datagramChannelSender.configureBlocking(true);
        datagramChannelSender.socket().bind(new InetSocketAddress(1024));
        datagramChannelSender.connect(new InetSocketAddress(InetAddress.getByName(ip), port));
    }

    int dropnum = 1;
    ArrayList<Integer> drop = getRadom(3);

    public void sendPacket(SenderPacket packetInfo) throws IOException {
        ByteBuffer will_send_packet = ByteBuffer.wrap(packetInfo.packet_info_bytes);
        if (drop.contains(sendNum)) {
            System.out.print("drop:" + (dropnum++) + " send:" + sendNum + " ");
            byte[] drop = new byte[packetInfo.general_nalu_bytes.length];
            for (int i = 0; i < packetInfo.general_nalu_bytes.length / 2; i++) {
                drop[i] = packetInfo.general_nalu_bytes[i];
            }
            packetInfo = new SenderPacket(packetInfo.gop_num, packetInfo.packet_num, packetInfo.sps_nalu_bytes, packetInfo.pps_nalu_bytes, packetInfo.general_nalu_start_bytes, drop);
            will_send_packet = ByteBuffer.wrap(packetInfo.packet_info_bytes);
            datagramChannelSender.write(will_send_packet);
        } else {
            datagramChannelSender.write(will_send_packet);
        }
        sendNum++;
        System.out.println("send:gop_num=" + packetInfo.gop_num + " packet_num=" + packetInfo.packet_num + " packet_header:26" + " start_code_size =" + packetInfo.general_nalu_start_bytes.length + " general_nalu_size=" + packetInfo.general_nalu_bytes.length + " packet_size=" + packetInfo.packet_info_bytes.length);
    }


    public void sendBytes(byte[] nalu_bytes) throws IOException, InterruptedException {
        Thread.sleep(0);
        ByteBuffer will_send_packet = ByteBuffer.wrap(nalu_bytes);
        System.out.println("send:" + (sendNum) + " size=" + nalu_bytes.length);
        if (drop.contains(sendNum) || sendNum == 0 || sendNum == 1) {
            System.out.println("drop:" + (dropnum++) + " send:" + sendNum);
            byte[] dropBytes = new byte[nalu_bytes.length];
            will_send_packet = ByteBuffer.wrap(dropBytes);
            datagramChannelSender.write(will_send_packet);
        } else {
            datagramChannelSender.write(will_send_packet);
        }
        sendNum++;
    }

    public ArrayList<Integer> getRadom(int num) {
        ArrayList<Integer> drop = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            drop.add((int) (Math.random() * 300));
        }
        return drop;
    }

}
