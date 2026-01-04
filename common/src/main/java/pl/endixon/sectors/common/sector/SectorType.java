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

package pl.endixon.sectors.common.sector;

import lombok.Getter;

@Getter
public enum SectorType {
    SPAWN,
    SECTOR,
    NETHER,
    END,
    QUEUE,
    AFK;



    public static boolean isQueueSector(String sectorName) {
        return sectorName != null && sectorName.equalsIgnoreCase(QUEUE.toString().toLowerCase());
    }
}
