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

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import pl.endixon.sectors.common.util.Corner;

@Getter
@Setter
public class SectorData implements Serializable {

    private  String name;
    private  Corner firstCorner;
    private  Corner secondCorner;
    private  String world;
    private  SectorType type;
    private  Corner center;
    private volatile boolean online;
    private volatile double tps;
    private volatile int playerCount;
    private volatile int maxPlayers;

    public SectorData(String name, Corner firstCorner, Corner secondCorner, String world, SectorType type) {
        this.name = name;
        this.firstCorner = firstCorner;
        this.secondCorner = secondCorner;
        this.world = world;
        this.type = type;
        this.center = new Corner(
  firstCorner.getPosX() + (secondCorner.getPosX() - firstCorner.getPosX()) / 2,
                0,
                firstCorner.getPosZ() + (secondCorner.getPosZ() - firstCorner.getPosZ()) / 2
        );
    }
}



