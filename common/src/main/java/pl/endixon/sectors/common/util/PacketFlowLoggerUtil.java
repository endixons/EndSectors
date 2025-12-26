/*
 *
 * EndSectors – Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 *
 */

package pl.endixon.sectors.common.util;

import lombok.Getter;
import pl.endixon.sectors.common.Common;

public final class PacketFlowLoggerUtil {

    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREY = "\u001B[90m";
    @Getter
    private boolean enabled = false;


    public void enable(boolean activateSniffer) {
        this.enabled = true;

        if (activateSniffer) {
            Common.getInstance().getNatsManager().enableNetworkSniffer();
            Common.getInstance().getLogger().info("PacketFlowLogger sniffer activated.");
        }
    }


    public void logSniffedPacket(String subject, String rawJson) {
        if (!this.enabled) {
            return;
        }
        String preview = rawJson.length() > 150 ? rawJson.substring(0, 150) + "... (trimmed)" : rawJson;
        String logMessage = CYAN + subject + RESET + GREY + " | " + preview + RESET;
        Common.getInstance().getLogger().info(logMessage);
    }
}