/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.virtdata.libbasics.shared.from_long.to_bytebuffer;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_long.Hash;

import java.nio.ByteBuffer;
import java.util.function.LongFunction;

/**
 * Hash a long input value into a byte buffer, at least length bytes long, but aligned on 8-byte
 * boundary;
 */
@ThreadSafeMapper
public class HashedToByteBuffer implements LongFunction<ByteBuffer> {

    private final Hash hash;
    private final int length;
    private final int bytes;
    private final int longs;

    public HashedToByteBuffer(int lengthInBytes) {
        this.length = lengthInBytes;
        this.hash = new Hash();
        this.longs = (length / Long.BYTES) +1;
        this.bytes = longs * Long.BYTES;
    }

    @Override
    public ByteBuffer apply(long input) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        for (int i = 0; i < longs; i++) {
            long l = hash.applyAsLong(input + i);
            buffer.putLong(l);
        }
        buffer.flip();
        return buffer;
    }

}
