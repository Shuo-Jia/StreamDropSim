package org.bjtu.iplab.js.receiver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class RecvSocket {
    private DatagramChannel datagramChannelRecver;

    public RecvSocket(int port) throws IOException {
        this.datagramChannelRecver = DatagramChannel.open();
        datagramChannelRecver.configureBlocking(true);
        datagramChannelRecver.socket().bind(new InetSocketAddress(port));
    }

    public byte[] recvPacket() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1500);
        datagramChannelRecver.receive(byteBuffer);
        byteBuffer.flip();
        byte[] recv_packet_info_bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(recv_packet_info_bytes);
        return recv_packet_info_bytes;
    }

    int seqNum = 0;

    public ByteBuffer recvByteBuffer() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1500);
        datagramChannelRecver.receive(byteBuffer);
        byteBuffer.flip();
        return byteBuffer;
    }
}
