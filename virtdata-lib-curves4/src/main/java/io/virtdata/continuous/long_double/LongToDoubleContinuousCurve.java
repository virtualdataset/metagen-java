package io.virtdata.continuous.long_double;

import io.virtdata.continuous.common.InterpolatingLongDoubleSampler;
import io.virtdata.continuous.common.RealDistributionICDSource;
import io.virtdata.continuous.common.RealLongDoubleSampler;
import org.apache.commons.statistics.distribution.ContinuousDistribution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;

/**
 * Generate samples according to the specified probability density.
 *
 * The input value consists of a long between 0L and Long.MAX_VALUE.
 * This value is scaled to the unit interval (0.0, 1.0) as
 * an index into a sampling function. The method used is
 * inverse cumulative density sampling.
 *
 * <H3>Sampling Mode</H3>
 *
 * The curve can be sampled in either map or hash mode. Map mode
 * simply indexes into the probability curve in the order that
 * it would appear on a density plot. Hash mode applies a
 * murmur3 hash to the input value before scaling from the
 * range of longs to the unit interval, thus providing a pseudo-random
 * sample of a value from the curve. This is usually what you want,
 * so hash mode is the default.  To enable map mode, simply provide
 * "map" as one of the modifiers as explained below.
 *
 * <H3>Interpolation</H3>
 *
 * The curve can be computed from the sampling function for each value
 * generated, or it can be provided via interpolation with a lookup table.
 * Using interpolation makes all the generator functions perform the
 * same. This is almost always what you want, so interpolation is
 * enabled by default. In order to compute the value for every sample
 * instead, simply provide "compute" as one of the modifiers as explained
 * below.
 *
 * You can add optional modifiers after the distribution parameters.
 * You can add one of 'hash' or 'map' but not both. If neither of these is
 * added, 'hash' is implied as a default.
 * You can add one of 'interpolate' or 'compute' but not both. If neither
 * of these is added, 'interpolate' is implied as a default.
 *
 * At times, it might be useful to add 'hash', 'interpolate' to your
 * specifiers as a form of verbosity or explicit specification.
 */

public class LongToDoubleContinuousCurve implements LongToDoubleFunction {

    private ContinuousDistribution distribution;
    private LongToDoubleFunction function;

    private final static HashSet<String> validModifiers = new HashSet<String>() {{
        add("compute");
        add("interpolate");
        add("map");
        add("hash");
    }};


    public LongToDoubleContinuousCurve(ContinuousDistribution distribution, String... modslist) {
        this.distribution = distribution;
        HashSet<String> mods = new HashSet<>(Arrays.asList(modslist));

        DoubleUnaryOperator icdSource = new RealDistributionICDSource(distribution);

        if (mods.contains("hash") && mods.contains("map")) {
            throw new RuntimeException("mods must not contain both hash and map.");
        }
        if (mods.contains("interpolate") && mods.contains("compute")) {
            throw new RuntimeException("mods must not contain both interpolate and compute");
        }
        for (String s : modslist) {
            if (!validModifiers.contains(s)) {
                throw new RuntimeException("modifier '" + s + "' is not a valid modifier. Use one of " + validModifiers.toString() + " instead.");
            }
        }


        boolean hash = ( mods.contains("hash") || !mods.contains("map"));
        boolean interpolate = ( mods.contains("interpolate") || !mods.contains("compute"));

        function = interpolate ?
                new InterpolatingLongDoubleSampler(icdSource, 1000, hash)
                :
                new RealLongDoubleSampler(icdSource, hash);
    }

    @Override
    public double applyAsDouble(long value) {
        return function.applyAsDouble(value);
    }
}
