package common;

import java.util.Objects;

public class GridPos implements java.io.Serializable {
    public final int x;
    public final int y;

    public GridPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public GridPos add(int dx, int dy) {
        return new GridPos(x + dx, y + dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof GridPos)) return false;
        GridPos gp = (GridPos) o;
        return this.x == gp.x && this.y == gp.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}