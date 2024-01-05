package ch.codingame.codevszombies;

import java.util.Objects;

public record Position(int x, int y) {

    public int squareDistanceFrom(Position other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return dx*dx+dy*dy;
    }

    public Position moveTo(Position target, int units) {
        Position direction = new Position(target.x - x, target.y - y);
        double length = Math.sqrt(direction.x * direction.x + direction.y * direction.y);
        double dx = direction.x / length;
        double dy = direction.y / length;
        return new Position((int)(x + dx * units), (int)(y + dy * units));
    }

    public Position clone() {
        return new Position(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
