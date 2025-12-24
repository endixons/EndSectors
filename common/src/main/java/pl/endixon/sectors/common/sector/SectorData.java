package pl.endixon.sectors.common.sector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import pl.endixon.sectors.common.util.Corner;

@Getter
@Setter
public class SectorData implements Serializable {

    private final String name;
    private final Corner firstCorner;
    private final Corner secondCorner;
    private final String world;
    private final SectorType type;
    private final Corner center;

    private boolean online;
    private double tps;
    private int playerCount;
    private int maxPlayers;


    @JsonCreator
    public SectorData(@JsonProperty("name") String name, @JsonProperty("firstCorner") Corner firstCorner, @JsonProperty("secondCorner") Corner secondCorner, @JsonProperty("world") String world, @JsonProperty("type") SectorType type) {
        this.name = name;
        this.firstCorner = firstCorner;
        this.secondCorner = secondCorner;
        this.world = world;
        this.type = type;
        this.center = new Corner(firstCorner.getPosX() + (secondCorner.getPosX() - firstCorner.getPosX()) / 2, 0, firstCorner.getPosZ() + (secondCorner.getPosZ() - firstCorner.getPosZ()) / 2);
    }


}
