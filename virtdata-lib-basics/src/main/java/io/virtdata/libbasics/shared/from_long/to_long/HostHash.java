package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.Example;
import org.greenrobot.essentials.hash.Murmur3F;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.LongUnaryOperator;

/**
 * Provides a stable hash that is seeded by all the interface
 * naming data on a given host. Three string properties from each known
 * interface on the system are used to create a stable seed value.
 * This is used to initialize the hash function each time before it is
 * used to generate a hash from the input long. Apart from the per-system
 * seeding, this hash function operates exactly the same as {@link Hash}
 */
public class HostHash implements LongUnaryOperator {

    private static long hostHash = computeHostHash();
    private ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
    private Murmur3F murmur3F;

    @Example({"HostHash()","a simple per-host hash function"})
    public HostHash() {
        murmur3F = new Murmur3F((int) hostHash % Integer.MAX_VALUE);
    }

    @Example({"HostHash(2343)","further permute the host hash with a specific seed"})
    public HostHash(int seedMod) {
        murmur3F = new Murmur3F((int) hostHash % Integer.MAX_VALUE);
        murmur3F.update(seedMod);
        murmur3F = new Murmur3F((int) murmur3F.getValue() & Integer.MAX_VALUE);
    }

    private static long computeHostHash() {
        InetAddress[] ifaces;
        try {
            Set<String> distinctNames = new HashSet<>();

            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                distinctNames.add(ni.getDisplayName());
                distinctNames.add(ni.getName());
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    distinctNames.add(inetAddress.getHostName());
                    distinctNames.add(inetAddress.getHostAddress());
                }
            }
            ifaces = InetAddress.getAllByName(null);
            Arrays.stream(ifaces).forEach(iface -> {
                distinctNames.add(iface.getCanonicalHostName());
                distinctNames.add(iface.getHostAddress());
                distinctNames.add(iface.getHostName());
            });
            List<String> nameList = new ArrayList<>(distinctNames);
            nameList.sort(String::compareTo);
            Murmur3F m3f = new Murmur3F(0);
            m3f.reset();
            distinctNames.forEach(
                    s -> m3f.update(s.getBytes(StandardCharsets.UTF_8))
            );
            return m3f.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long applyAsLong(long value) {
        murmur3F.reset();
        bb.putLong(0, value);
        murmur3F.update(bb.array(), 0, Long.BYTES);
        long result = Math.abs(murmur3F.getValue());
        return result;
    }
}
