package pl.endixon.sectors.paper.util;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public final class CpuUtil {


    private static final OperatingSystemMXBean OS_BEAN = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private CpuUtil() {
    }


    public static double getProcessCpuLoad() {
        double load = OS_BEAN.getProcessCpuLoad();
        if (load < 0) {
            return 0.0;
        }
        return Math.round(load * 10000.0) / 100.0;
    }
}