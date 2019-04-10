package org.bjtu.iplab.js.sender;

import org.bjtu.iplab.js.StreamInfo.NaluInfo;

import java.io.*;

public class NaluParseFile {


    private RandomAccessFile fileInputRandomStream;
    private boolean isAvaliable = true;
    private NaluInfo naluInfo;
    private byte[] tempReadBuf = new byte[1024000];

    public NaluParseFile(String fileUrl) throws FileNotFoundException {
        fileInputRandomStream = new RandomAccessFile(fileUrl, "rw");
    }

    int pos = 0;
    public byte[] getMTUpayload() throws IOException {
        byte[] temp = new byte[1400];
        int res = 1;
        int offset = 0;
        while (res == 1 && offset != 1400) {
            res = fileInputRandomStream.read(temp, offset, 1);
            offset++;
            pos++;
        }
        if (res != 1) {

            isAvaliable = false;
        }
        fileInputRandomStream.seek(pos);
        return temp;
    }

    public NaluInfo getNextNalu(int pre_end_offset) throws IOException {
        naluInfo = new NaluInfo();

        boolean nextStartCodeFound = false;
        int tempBuf_pos = 0;

        naluInfo.startLen = 3;
        naluInfo.offset = pre_end_offset;


        if (fileInputRandomStream.read(tempReadBuf, tempBuf_pos, 3) != 3) {
            return new NaluInfo();
        } else {
            if (findStartCode3(tempReadBuf, tempBuf_pos)) {
                tempBuf_pos = 3;
                naluInfo.startLen = 3;
            } else {
                if (fileInputRandomStream.read(tempReadBuf, tempBuf_pos + 3, 1) != 1) {
                    return new NaluInfo();
                } else {
                    tempBuf_pos = 4;
                    naluInfo.startLen = 4;
                }
            }
        }


        while (!nextStartCodeFound) {
            if (fileInputRandomStream.read(tempReadBuf, tempBuf_pos++, 1) == -1) {
                isAvaliable = false;

                naluInfo.length_with_start = tempBuf_pos;
                naluInfo.startload = new byte[naluInfo.startLen];
                naluInfo.payload = new byte[naluInfo.length_with_start -naluInfo.startLen];
                naluInfo.allDataload = new byte[naluInfo.length_with_start];
                System.arraycopy(tempReadBuf,0,naluInfo.startload,0,naluInfo.startLen);
                System.arraycopy(tempReadBuf, naluInfo.startLen, naluInfo.payload, 0, naluInfo.length_with_start -naluInfo.startLen);
                System.arraycopy(tempReadBuf,0,naluInfo.allDataload,0,naluInfo.length_with_start);
                naluInfo.forbidden_bit = naluInfo.payload[0] & 0x80;
                naluInfo.nal_reference_idc = naluInfo.payload[0] & 0x60;
                naluInfo.nal_unit_type = naluInfo.payload[0] & 0x1f;
                return naluInfo;
            }

            boolean s4 = findStartCode4(tempReadBuf, tempBuf_pos - 4);
            boolean s3 = findStartCode3(tempReadBuf, tempBuf_pos - 3);
            nextStartCodeFound = s4 || s3;
        }

        tempBuf_pos = (findStartCode4(tempReadBuf, tempBuf_pos - 4)) ? tempBuf_pos - 4 : tempBuf_pos - 3;

        fileInputRandomStream.seek(naluInfo.offset + tempBuf_pos);

        naluInfo.length_with_start = tempBuf_pos;
        naluInfo.startload = new byte[naluInfo.startLen];
        naluInfo.payload = new byte[naluInfo.length_with_start -naluInfo.startLen];
        naluInfo.allDataload = new byte[naluInfo.length_with_start];
        System.arraycopy(tempReadBuf,0,naluInfo.startload,0,naluInfo.startLen);
        System.arraycopy(tempReadBuf, naluInfo.startLen, naluInfo.payload, 0, naluInfo.length_with_start -naluInfo.startLen);
        System.arraycopy(tempReadBuf,0,naluInfo.allDataload,0,naluInfo.length_with_start);
        naluInfo.forbidden_bit = naluInfo.payload[0] & 0x80;
        naluInfo.nal_reference_idc = naluInfo.payload[0] & 0x60;
        naluInfo.nal_unit_type = naluInfo.payload[0] & 0x1f;
        return naluInfo;
    }

    public boolean isAvaliable() throws IOException {
        return isAvaliable;
    }

    private boolean findStartCode3(byte[] bytes, int pos) {
        return !((bytes[pos] != 0) || (bytes[pos + 1] != 0) || (bytes[pos + 2] != 1));
    }

    private boolean findStartCode4(byte[] bytes, int pos) {
        return !((bytes[pos] != 0) || (bytes[pos + 1] != 0) || (bytes[pos + 2] != 0) || (bytes[pos + 3] != 1));
    }
}
