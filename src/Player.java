import java.util.*;

/**
 * Save humans, destroy zombies!
 **/
class Player {

    public static int ASH_RADIUS_SQ = 2000*2000;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Human toSave = null;

        final List<Human> humans = new ArrayList<>();
        final Set<Integer> humanIds = new HashSet<>();
        final List<Zombie> zombies = new ArrayList<>();

        // game loop
        while (true) {
            humans.clear();
            zombies.clear();
            int x = in.nextInt();
            int y = in.nextInt();
            int humanCount = in.nextInt();
            for (int i = 0; i < humanCount; i++) {
                int humanId = in.nextInt();
                int humanX = in.nextInt();
                int humanY = in.nextInt();
                humans.add(new Human(humanId, humanX, humanY));
                humanIds.add(humanId);
            }
            int zombieCount = in.nextInt();
            for (int i = 0; i < zombieCount; i++) {
                int zombieId = in.nextInt();
                int zombieX = in.nextInt();
                int zombieY = in.nextInt();
                int zombieXNext = in.nextInt();
                int zombieYNext = in.nextInt();
                zombies.add(new Zombie(zombieId, zombieX, zombieY, zombieXNext, zombieYNext));
            }

            // zombies.sort((o1, o2) -> o1.squareDistanceFrom(x, y) > o2.squareDistanceFrom(x, y) ? 1 : -1);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            if (zombies.size() == 1) {
                System.out.println(zombies.get(0).positionToString());
            } else {
                Centroid c = calculateCentroid(humans);
                Centroid zombieCentroid = calculateCentroid(zombies);

                if (zombieCentroid.covers() >= 0.9 && !zombies.isEmpty()) {
                    // ash can kill at least 90% of the zombies at the same time
                    System.out.println(zombies.get(0).positionToString());
                } else if (humans.size() == 1 || c.coversAll) {
                    // if there's only one human or ash can protect them all at once, go kill zombies
                    Map<Human, ZombieDistance> nearsZombieDistances = getNearestZombieDistances(humans, zombies);
                    System.out.println(nearsZombieDistances.get(humans.get(0)).zombie.positionToString());
                } else if (c.coversAny) {
                    // if the centroid covers any human, use it
                    // if not, try to find human to save and protect him
                    System.out.println(c.positionToString());
                } else {
                    Map<Human, ZombieDistance> nearsZombieDistances = getNearestZombieDistances(humans, zombies);
                    Human h = getHumanNearestZombie(nearsZombieDistances);
                    int myDistance = h.squareDistanceFrom(x, y);

                    // if I can reach the human with nearest zombie before the zombie 
                    // eats him, kill the zombie
                    // if not, just try to protect human furthest from his nearest zombie
                    if (nearsZombieDistances.get(h).distance / 400 > myDistance / 1000) {
                        System.out.println(nearsZombieDistances.get(h).zombie.positionToString());
                    } else {
                        // try to preserve toSave over iterations of game loop
                        if (toSave == null || !humanIds.contains(toSave.id)) {
                            toSave = getHumanFurthestFromZombies(nearsZombieDistances);
                        }
                        System.out.println(toSave.positionToString());
                    }
                }
            }

        }
    }


    public static Centroid calculateCentroid(List<? extends IHasPosition> items) {
        int cx = 0, cy = 0;
        for(IHasPosition item : items) {
            cx += item.getX();
            cy += item.getY();
        }

        cx /= items.size();
        cy /= items.size();

        int covered = 0;

        for(IHasPosition item : items) {
            if (item.squareDistanceFrom(cx, cy) < ASH_RADIUS_SQ) {
                covered++;
            }
        }

        return new Centroid(cx, cy, items.size(), covered);
    }

    public static Map<Human, ZombieDistance> getNearestZombieDistances(List<Human> humans, List<Zombie> zombies) {
        Map<Human, ZombieDistance> nearestZombieDistances = new HashMap<>();
        for(Human human : humans) {
            int minDist = -1;
            Zombie nearestZombie = null;
            for(Zombie zombie : zombies) {
                int d = human.squareDistanceFromZombie(zombie);
                if (nearestZombie == null || d < minDist) {
                    minDist = d;
                    nearestZombie = zombie;
                }
            }
            nearestZombieDistances.put(human, new ZombieDistance(minDist, nearestZombie));
        }

        return nearestZombieDistances;
    }

    public static Human getHumanNearestZombie(Map<Human, ZombieDistance> nearestZombieDistances) {
        // select which human is the furthest from nearest zombies
        int minDist = -1;
        Human h = null;
        for (Human human : nearestZombieDistances.keySet()) {
            Integer d = nearestZombieDistances.get(human).distance;
            if (h == null || d < minDist) {
                minDist = d;
                h = human;
            }
        }

        return h;
    }

    public static Human getHumanFurthestFromZombies(Map<Human, ZombieDistance> nearestZombieDistances) {
        // select which human is the furthest from nearest zombies
        int maxDist = -1;
        Human h = null;
        for (Human human : nearestZombieDistances.keySet()) {
            Integer d = nearestZombieDistances.get(human).distance;
            if (d > maxDist) {
                maxDist = d;
                h = human;
            }
        }

        return h;
    }

    public static class Centroid {

        private final int total;
        private final int covered;

        public final int x;
        public final int y;
        public final boolean coversAny;
        public final boolean coversAll;

        public Centroid(int x, int y, int totalItems, int coveredItems) {
            this.x = x;
            this.y = y;
            this.coversAny = coveredItems > 0;
            this.coversAll = coveredItems == totalItems;
            total = totalItems;
            covered = coveredItems;
        }

        public String positionToString() {
            return x+" "+y;
        }

        public boolean isCoversAny() {
            return coversAny;
        }

        public boolean isCoversAll() {
            return coversAll;
        }

        public double covers() {
            return covered/(double)total;
        }
    }

    private interface IHasPosition {
        int getX();
        int getY();

        int squareDistanceFrom(int ox, int oy);
    }

    private static class Human implements IHasPosition {

        public final int id;
        public final int x;
        public final int y;

        public Human(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        public int squareDistanceFromZombie(Zombie zombie) {
            int dx = this.x - zombie.x;
            int dy = this.y - zombie.y;
            return dx*dx+dy*dy;
        }

        public int squareDistanceFrom(int otherX, int otherY) {
            int dx = this.x - otherX;
            int dy = this.y - otherY;
            return dx*dx+dy*dy;
        }

        public String positionToString() {
            return x+" "+y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }

    private static class ZombieDistance {
        public final int distance;
        public final Zombie zombie;

        public ZombieDistance(int distance, Zombie zombie) {
            this.distance = distance;
            this.zombie = zombie;
        }
    }

    private static class Zombie implements IHasPosition {
        public final int id;
        public final int x;
        public final int y;
        public final int nextX;
        public final int nextY;

        public Zombie(int id, int x, int y, int nextX, int nextY) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.nextX = nextX;
            this.nextY = nextY;
        }

        public String positionToString() {
            return x+" "+y;
        }

        public int squareDistanceFrom(int otherX, int otherY) {
            int dx = this.x - otherX;
            int dy = this.y - otherY;
            return dx*dx+dy*dy;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }
}