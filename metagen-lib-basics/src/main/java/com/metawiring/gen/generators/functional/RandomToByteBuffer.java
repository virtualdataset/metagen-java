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

package com.metawiring.gen.generators.functional;

import com.metawiring.gen.metagenapi.Generator;
import org.apache.commons.math3.random.MersenneTwister;

import java.nio.ByteBuffer;

public class RandomToByteBuffer implements Generator<ByteBuffer> {

    private MersenneTwister twister = new MersenneTwister();
    private int length;

    public RandomToByteBuffer(int length) {
        this.length = length;
    }

    @Override
    public ByteBuffer get(long input) {
        byte[] buffer = new byte[length];
        twister.nextBytes(buffer);
        return ByteBuffer.wrap(buffer);
    }

}
