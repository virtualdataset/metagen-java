/*
*   Copyright 2016 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
package io.virtdata.libbasics.shared.from_long.to_int;

import io.virtdata.annotations.Description;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@Description("divides the operand by an long and returns the whole part")
@ThreadSafeMapper
public class Div implements LongToIntFunction {
    private int divisor;

    public Div(int divisor) {
        this.divisor = divisor;
    }

    @Override
    public int applyAsInt(long operand) {
        return (int) ((operand / divisor) & Integer.MAX_VALUE);
    }
}
