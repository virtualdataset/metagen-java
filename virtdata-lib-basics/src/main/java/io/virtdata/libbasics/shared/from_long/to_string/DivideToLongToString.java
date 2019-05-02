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

package io.virtdata.libbasics.shared.from_long.to_string;

import io.virtdata.annotations.DeprecatedFunction;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

/**
 * This is equivalent to <pre>Div(...)</pre>, but returns
 * the result after String.valueOf(...). This function is also deprecated,
 * as it is easily replaced by other functions.
 */
@ThreadSafeMapper
@DeprecatedFunction("This function is easily replace by simpler functions.")
public class DivideToLongToString implements LongFunction<String> {

    private final long divisor;
    AtomicLong seq=new AtomicLong(0);

    public DivideToLongToString(long divisor) {
        this.divisor=divisor;
    }

    @Override
    public String apply(long operand) {
        return String.valueOf((operand / divisor));
    }
}