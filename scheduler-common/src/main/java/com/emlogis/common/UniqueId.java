package com.emlogis.common;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.eaio.uuid.UUIDGen;

public class UniqueId {

    private volatile int seq;
    private volatile long lastTimestamp;
    private final Object lock = new Object();
    private final int maxShort = (int) 0xffff;

    // Get the MAC address (i.e., the "node" from a UUID1)
    private final long clockSeqAndNode = UUIDGen.getClockSeqAndNode();

    private final byte[] node = new byte[] {
            (byte) ((clockSeqAndNode >> 40) & 0xff),
            (byte) ((clockSeqAndNode >> 32) & 0xff),
            (byte) ((clockSeqAndNode >> 24) & 0xff),
            (byte) ((clockSeqAndNode >> 16) & 0xff),
            (byte) ((clockSeqAndNode >> 8) & 0xff),
            (byte) ((clockSeqAndNode >> 0) & 0xff),
    };

    private final ThreadLocal<ByteBuffer> tlbb = new ThreadLocal<ByteBuffer>() {
        @Override
        public ByteBuffer initialValue() {
            return ByteBuffer.allocate(16);
        }
    };

    private static UniqueId uniqueId = new UniqueId();

    public static String getId() {
        return uniqueId.generateId();
    }

    private byte[] generateByteArray() {
        if (seq == maxShort) {
            throw new RuntimeException("Too fast");
        }

        long time;
        synchronized (lock) {
            time = System.currentTimeMillis();
            if (time != lastTimestamp) {
                lastTimestamp = time;
                seq = 0;
            }
            seq++;
            ByteBuffer bb = tlbb.get();
            bb.rewind();
            bb.putLong(time);
            bb.put(node);
            bb.putShort((short) seq);
            return bb.array();
        }
    }

    private String generateId() {
        byte[] ba = generateByteArray();
        BigInteger val = new BigInteger(ba);
        return val.toString(36);
    }

}

