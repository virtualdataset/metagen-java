package io.virtdata.long_long;

import de.greenrobot.common.hash.Murmur3F;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

/**
 * This uses the Murmur3F (64-bit optimized) version of Murmur3,
 * not as a checksum, but as a simple hash. It doesn't bother
 * pushing the high-64 bits of input, since it only uses the lower
 * 64 bits of output.
 *
 * This version returns the value regardless of this sign bit.
 * It does not return the absolute value, as {@link Hash} does.
 */
public class FullHash implements LongUnaryOperator {

    private ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
    private Murmur3F murmur3F= new Murmur3F();

    @Override
    public long applyAsLong(long value) {
        murmur3F.reset();
        bb.putLong(0,value);
//        bb.position(0);
        murmur3F.update(bb.array(),0,Long.BYTES);
        return murmur3F.getValue();
    }
}
