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
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Corner implements Serializable {

    private int posX;
    private int y;
    private int posZ;

    public Corner(int posX, int y, int posZ) {
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
