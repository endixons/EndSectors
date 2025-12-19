package pl.endixon.sectors.tools.service.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Home {

    private String name;
    private String sector;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
}
