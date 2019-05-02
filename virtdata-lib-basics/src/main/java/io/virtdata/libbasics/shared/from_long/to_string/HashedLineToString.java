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

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_int.HashRange;
import io.virtdata.util.ResourceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Return a pseudo-randomly selected String value from a single line of
 * the specified file.
 */
@ThreadSafeMapper
public class HashedLineToString implements LongFunction<String> {
    private final static Logger logger = LoggerFactory.getLogger(HashedLineToString.class);
    private final HashRange indexRange;

    private List<String> lines = new ArrayList<>();

    private final String filename;

    public HashedLineToString(String filename) {
        this.filename = filename;
        this.lines = ResourceFinder.readDataFileLines(filename);
        this.indexRange = new HashRange(0, lines.size()-2);
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }

    @Override
    public String apply(long operand) {
        int itemIdx = indexRange.applyAsInt(operand);
        String item = lines.get(itemIdx);
        return item;
    }

}
