package io.virtdata.libimpl.continuous.impl.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.NakagamiDistribution;

/**
 * {@inheritDoc}
 *
 * @see io.virtdata.libimpl.continuous.impl.long_double.LongToDoubleContinuousCurve
 */
@ThreadSafeMapper
public class Nakagami extends LongToDoubleContinuousCurve {
    public Nakagami(double mu, double omega, String... mods) {
        super(new NakagamiDistribution(mu, omega), mods);
    }
}