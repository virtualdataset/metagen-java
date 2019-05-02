package io.virtdata.libbasics.shared.from_long.to_uuid;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_long.Hash;

import java.util.UUID;
import java.util.function.LongFunction;

/**
 * This function provides a stable hashing of the input value to
 * a version 4 (Random) UUID.
 */
@ThreadSafeMapper
public class ToHashedUUID implements LongFunction<UUID> {

    private Hash longHash = new Hash();

    @Override
    public UUID apply(long value) {
        long msbs = longHash.applyAsLong(value);
        long lsbs = longHash.applyAsLong(value+1);
        msbs = (msbs & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L; // It's not worth doing byte-level ops.
        lsbs = (lsbs & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L; // It all gets handled as longs anyway.
        UUID uuid= new UUID(msbs,lsbs);
        return uuid;
    }
}
