package org.bjtu.iplab.js.StreamInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PacketInfo {
    public int gop_num;
    public int packet_num;

    public transient byte[] header_bytes;
    public transient byte[] sps_nalu_bytes;
    public transient byte[] pps_nalu_bytes;
    public transient byte[] general_nalu_bytes;
    public transient byte[] packet_info_bytes;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE,false);
    }
}
