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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.Getter;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;

@Getter
public class SectorManager {

    private final List<SectorData> sectorsData = new ArrayList<>();

    public void addSectorData(SectorData sectorData) {
        this.sectorsData.add(sectorData);
    }

    public SectorData getSectorData(String sectorName) {
        for (SectorData sectorData : this.sectorsData) {
            if (sectorData.getName().equalsIgnoreCase(sectorName)) {
                return sectorData;
            }
        }

        return null;
    }

    public List<SectorData> getNonQueueSectors() {
        List<SectorData> list = new ArrayList<>();
        for (SectorData data : sectorsData) {
            if (data.getType() != SectorType.QUEUE) {
                list.add(data);
            }
        }
        return list;
    }

    public Optional<SectorData> getRandomNonQueueSector() {

        List<SectorData> allowedSectors = getNonQueueSectors().stream().filter(sector -> sector.getType() != SectorType.QUEUE && sector.getType() != SectorType.NETHER && sector.getType() != SectorType.END).toList();

        if (allowedSectors.isEmpty())
            return Optional.empty();

        List<SectorData> onlineSectors = allowedSectors.stream().filter(SectorData::isOnline).toList();

        List<SectorData> targetList = onlineSectors.isEmpty() ? allowedSectors : onlineSectors;
        return Optional.of(targetList.get(new Random().nextInt(targetList.size())));
    }
}
