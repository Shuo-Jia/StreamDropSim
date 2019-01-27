package org.bjtu.iplab.js.sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class SendSocket {

    private int sendNum = 0;
    private DatagramChannel datagramChannelSender;

    public SendSocket(String ip, int port) throws IOException {
        this.datagramChannelSender = DatagramChannel.open();
        datagramChannelSender.configureBlocking(true);
        datagramChannelSender.socket().bind(new InetSocketAddress(1024));
        datagramChannelSender.connect(new InetSocketAddress(InetAddress.getByName(ip), port));
    }

    public void sendPacket(SenderPacket packetInfo) throws IOException {
        ByteBuffer will_send_packet = ByteBuffer.wrap(packetInfo.packet_info_bytes);
        datagramChannelSender.write(will_send_packet);
        System.out.println("send:gop_num=" + packetInfo.gop_num + " packet_num=" + packetInfo.packet_num + " packet_size=" + packetInfo.packet_info_bytes.length + " general_nalu_size=" + packetInfo.general_nalu_bytes.length);
    }

    public void sendBytes(byte[] nalu_bytes) throws IOException {
        ByteBuffer will_send_packet = ByteBuffer.wrap(nalu_bytes);
        datagramChannelSender.write(will_send_packet);
        System.out.println("send:" + (sendNum++) + " size=" + nalu_bytes.length);
    }

}
