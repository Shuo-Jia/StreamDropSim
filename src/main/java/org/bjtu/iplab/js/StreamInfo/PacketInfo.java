package org.bjtu.iplab.js.StreamInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PacketInfo {
    public int gop_num;
    public int packet_num;

    public transient byte[] header_bytes;
    public transient byte[] sps_nalu_bytes;
    public transient byte[] pps_nalu_bytes;
    public transient byte[] general_nalu_start_bytes;
    public transient byte[] general_nalu_bytes;
    public transient byte[] packet_info_bytes;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE,false);
    }

    public int getGop_num() {
        return gop_num;
    }

    public void setGop_num(int gop_num) {
        this.gop_num = gop_num;
    }

    public int getPacket_num() {
        return packet_num;
    }

    public void setPacket_num(int packet_num) {
        this.packet_num = packet_num;
    }

    public byte[] getHeader_bytes() {
        return header_bytes;
    }

    public void setHeader_bytes(byte[] header_bytes) {
        this.header_bytes = header_bytes;
    }

    public byte[] getSps_nalu_bytes() {
        return sps_nalu_bytes;
    }

    public void setSps_nalu_bytes(byte[] sps_nalu_bytes) {
        this.sps_nalu_bytes = sps_nalu_bytes;
    }

    public byte[] getPps_nalu_bytes() {
        return pps_nalu_bytes;
    }

    public void setPps_nalu_bytes(byte[] pps_nalu_bytes) {
        this.pps_nalu_bytes = pps_nalu_bytes;
    }

    public byte[] getGeneral_nalu_bytes() {
        return general_nalu_bytes;
    }

    public void setGeneral_nalu_bytes(byte[] general_nalu_bytes) {
        this.general_nalu_bytes = general_nalu_bytes;
    }

    public byte[] getPacket_info_bytes() {
        return packet_info_bytes;
    }

    public void setPacket_info_bytes(byte[] packet_info_bytes) {
        this.packet_info_bytes = packet_info_bytes;
    }
}
