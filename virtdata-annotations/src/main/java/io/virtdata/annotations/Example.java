package io.virtdata.annotations;


import java.lang.annotation.*;
import java.util.regex.Pattern;

/**
 * The example annotation allows for a function developer to attach illustrative
 * examples for any given constructor. You can have multiple
 * <pre>@Example</pre> annotation per constructor.
 *
 *
 * A few key formats are allowed
 */
@Repeatable(value = Examples.class)
public @interface Example {
    String[] value();

}
