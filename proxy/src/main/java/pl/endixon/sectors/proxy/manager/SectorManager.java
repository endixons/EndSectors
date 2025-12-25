/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.proxy.manager;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;

@Getter
public class SectorManager {

    private final List<SectorData> sectorsData = new CopyOnWriteArrayList<>();
    private static final Random RANDOM = new Random();

    public void addSectorData(final SectorData sectorData) {
        if (sectorData != null) {
            this.sectorsData.add(sectorData);
        }
    }

    public void clear() {
        this.sectorsData.clear();
    }

    public SectorData getSectorData(final String sectorName) {
        for (final SectorData sectorData : this.sectorsData) {
            if (sectorData.getName().equalsIgnoreCase(sectorName)) {
                return sectorData;
            }
        }
        return null;
    }

    public Optional<SectorData> getRandomNonQueueSector() {
        final List<SectorData> allowedSectors = this.sectorsData.stream()
                .filter(sector -> sector.getType() != SectorType.QUEUE
                        && sector.getType() != SectorType.NETHER
                        && sector.getType() != SectorType.END)
                .toList();

        if (allowedSectors.isEmpty()) {
            return Optional.empty();
        }

        final List<SectorData> onlineSectors = allowedSectors.stream()
                .filter(SectorData::isOnline)
                .toList();

        final List<SectorData> targetList = onlineSectors.isEmpty() ? allowedSectors : onlineSectors;
        return Optional.of(targetList.get(RANDOM.nextInt(targetList.size())));
    }
}