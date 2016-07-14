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

package io.virtdata.functional;

import java.util.function.LongUnaryOperator;

/**
 * Integer devide the cycle, the other side of modulo.
 */
public class DivideToLong implements LongUnaryOperator {

    private final long divisor;

    public DivideToLong(long divisor) {
        this.divisor=divisor;
    }
    public DivideToLong(String divisor) {
        this(Long.valueOf(divisor));
    }

    @Override
    public long applyAsLong(long input) {
        return (input / divisor);
    }
}