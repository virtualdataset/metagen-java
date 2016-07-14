package io.virtdata.gen.internal;

import de.greenrobot.common.hash.Murmur3F;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

/**
 * This uses the Murmur3F (64-bit optimized) version of Murmur3,
 * not as a checksum, but as a simple hash. It doesn't bother
 * pushing the high-64 bits of input, since it only uses the lower
 * 64 bits of output. It does, however, return the absolute value.
 * This is to make it play nice with users and other libraries.
 */
public class Murmur3Hash implements LongUnaryOperator {

    private ThreadLocal<ByteBuffer> tlbb = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(Long.BYTES);
        }
    };

    private Murmur3F murmur3F= new Murmur3F();

    @Override
    public long applyAsLong(long value) {
        ByteBuffer bb = tlbb.get();
        murmur3F.reset();
        bb.clear();
        bb.putLong(value);
        bb.flip();
        murmur3F.update(bb.array());
        long result= Math.abs(murmur3F.getValue());
        return result;
    }
}
