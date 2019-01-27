package org.bjtu.iplab.js.StreamInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class NaluInfo {
    public int offset;
    public int startLen;      //! 4 for parameter sets and first slice in picture, 3 for everything else (suggested)
    public int length;                 //! Length of the NAL unit (Excluding the start code, which does not belong to the NALU)

    public int forbidden_bit;            //! should be always FALSE
    public int nal_reference_idc;        //! NALU_PRIORITY_xxxx
    public int nal_unit_type;            //! NALU_TYPE_xxxx

    public transient byte[] payload;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE,false);
    }
}
