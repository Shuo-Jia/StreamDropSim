package org.bjtu.iplab.js.sender;

import org.bjtu.iplab.js.StreamInfo.PacketInfo;

public class SenderPacket extends PacketInfo {

    public SenderPacket(int send_gop_num, int send_packet_num, byte[] send_sps_nalu_bytes, byte[] send_pps_nalu_bytes, byte[] send_general_nalu_bytes) {
        this.gop_num = send_gop_num;
        this.packet_num = send_packet_num;

        this.header_bytes = new byte[4];
        this.sps_nalu_bytes = send_sps_nalu_bytes;
        this.pps_nalu_bytes = send_pps_nalu_bytes;
        this.general_nalu_bytes = send_general_nalu_bytes;

        header_bytes[0] = (byte) (send_gop_num >> 8);
        header_bytes[1] = (byte) (send_gop_num & 0xFF);

        header_bytes[2] = (byte) (send_packet_num >> 8);
        header_bytes[3] = (byte) (send_packet_num & 0xFF);

        packet_info_bytes = new byte[header_bytes.length + sps_nalu_bytes.length + pps_nalu_bytes.length + general_nalu_bytes.length];
        System.arraycopy(header_bytes, 0, packet_info_bytes, 0, header_bytes.length);
        System.arraycopy(sps_nalu_bytes, 0, packet_info_bytes, header_bytes.length, sps_nalu_bytes.length);
        System.arraycopy(pps_nalu_bytes, 0, packet_info_bytes, header_bytes.length + sps_nalu_bytes.length, pps_nalu_bytes.length);
        System.arraycopy(general_nalu_bytes, 0, packet_info_bytes, header_bytes.length + sps_nalu_bytes.length + pps_nalu_bytes.length, general_nalu_bytes.length);
    }

}
