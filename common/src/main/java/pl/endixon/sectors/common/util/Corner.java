package pl.endixon.sectors.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class Corner implements Serializable {

    private int posX;
    private int y;
    private int posZ;

    @JsonCreator
    public Corner(@JsonProperty("posX") int posX, @JsonProperty("y") int y, @JsonProperty("posZ") int posZ) {
        this.posX = posX;
        this.y = y;
        this.posZ = posZ;
    }

    public Corner(int posX, int posZ) {
        this(posX, 0, posZ);
    }

    public Corner() {
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPosZ() {
        return posZ;
    }

    public void setPosZ(int posZ) {
        this.posZ = posZ;
    }

    @Override
    public String toString() {
        return "Corner{posX=" + posX + ", y=" + y + ", posZ=" + posZ + '}';
    }
}
