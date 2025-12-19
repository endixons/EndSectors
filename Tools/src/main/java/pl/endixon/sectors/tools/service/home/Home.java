package pl.endixon.sectors.tools.service.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Home {

    private String name;
    private String sector;
    private String world;

    private double x;
    private double y;
    private double z;

    private float yaw;
    private float pitch;

    public Home(String name, String sector, String world,
                double x, double y, double z,
                float yaw, float pitch) {

        this.name = name;
        this.sector = sector;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
