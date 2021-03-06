package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GammaDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Gamma_distribution">Wikipedia: Gamma distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/GammaDistribution.html">Commons JavaDoc: GammaDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Gamma extends IntToDoubleContinuousCurve {
    public Gamma(double shape, double scale, String... mods) {
        super(new GammaDistribution(shape, scale), mods);
    }
}
