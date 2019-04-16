package io.virtdata.long_timeuuid;

import io.virtdata.basicsmappers.from_long.to_time_types.ToEpochTimeUUID;
import io.virtdata.basicsmappers.from_long.to_time_types.ToFinestTimeUUID;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeUUIDTests {

    private final DateTime calStart = new DateTime(
            1582,10,15,
            0,0,0,
            DateTimeZone.UTC
    );
    private final DateTime aYearLater = new DateTime(
            1583, 10, 15,
            0, 0, 0,
            DateTimeZone.UTC
    );

    private final DateTime refTime = new DateTime(
            2015, 5, 11,
            23, 23, 23,
            DateTimeZone.UTC
    );

    private final DateTime may5 = new DateTime(
            2015,5,11,23,23,23,
            DateTimeZone.UTC
    );
    private final DateTime may5aYearLater = new DateTime(
            2016, 5, 11, 23, 23, 23,
            DateTimeZone.UTC
    );
    private final String calStartSpec = "1582-10-15 00:00:00";
    private final String aYearLaterSpec = "1583-10-15 00:00:00";
    private final String may5Spec = "2015-05-11 23:23:23";
    private final String may5aYearLaterSpec = "2016-05-11 23:23:23";


    @Test
    public void testFinestTimeUUID() {
        ToFinestTimeUUID tjtu = new ToFinestTimeUUID(234,567);
        UUID uuid = tjtu.apply(0);
        assertThat(uuid.node()).isEqualTo(234);
        assertThat(uuid.clockSequence()).isEqualTo(567);
        assertThat(uuid.timestamp()).isEqualTo(0L);
        uuid = tjtu.apply(5);
        assertThat(uuid.timestamp()).isEqualTo(5L);
    }

    @Test
    public void testEpochTimeUUID() {
        ToEpochTimeUUID tetu = new ToEpochTimeUUID(123,456);
        UUID uuid = tetu.apply(0);
        assertThat(uuid.node()).isEqualTo(123L);
        assertThat(uuid.clockSequence()).isEqualTo(456);
        DateTime gregs =
                new DateTime(
                        1582,
                        10,
                        15,
                        0,
                        0,
                        DateTimeZone.UTC);
        Interval interval = new Interval(gregs, new DateTime(0));
        long expected = interval.toDuration().getMillis() * 10000;
        assertThat(uuid.timestamp()).isEqualTo(expected);
    }

    /**
     * This test measures the timeuuid timestamp interval
     * between two known points in time, based on epoch millis.
     * It verifies that the rate of change between the millis
     * and the timeuuid interval of 100ns is correct.
s     */
    @Test
    public void testEpochBaseTimeUUID() {

        ToEpochTimeUUID basedIn2015 = new ToEpochTimeUUID(may5Spec);
        Duration ayear = new Duration(may5,may5aYearLater);
        long ayearMillis = ayear.getMillis();
        UUID refYear = basedIn2015.apply(0);
        long refTicks = refYear.timestamp();
        UUID nextYear = basedIn2015.apply(ayearMillis);
        long nextTicks = nextYear.timestamp();
        long expectedTicks = ayearMillis*10000;
        long actualTicks = nextTicks - refTicks;
        assertThat(actualTicks).isEqualTo(expectedTicks);
    }

    @Test
    public void testFinestBaseTimeUUID() {
        ToFinestTimeUUID tetu = new ToFinestTimeUUID(calStartSpec);
        UUID uuid = tetu.apply(0);
        assertThat(uuid.timestamp()).isEqualTo(0L);
        uuid = tetu.apply(2);
        assertThat(uuid.timestamp()).isEqualTo(2L);
    }

    @Test
    public void sanityCheckNoHostGen() {
        ToFinestTimeUUID tjtu = new ToFinestTimeUUID();
        UUID withHostData = tjtu.apply(0);
        assertThat(withHostData.node()).isEqualTo(0L);
    }
}