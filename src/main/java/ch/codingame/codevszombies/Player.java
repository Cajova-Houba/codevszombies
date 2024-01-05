import java.util.*;

/**
 * Save humans, destroy zombies!
 **/
class Player {

    public static int ASH_RADIUS_SQ = 2000*2000;

    public static int W = -1;
    public static int H = -1;

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
            W = in.nextInt();
            H = in.nextInt();
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

            int time = 1000000;
            int seed = 1;
            int bestScore = -1;
            int id = 1;
            Strategy bestStrategy = null;
            for (int i = 0; i < time; i++) {
                Strategy strategy = generateRandomStrategy(id++, seed, zombies);
                int score = strategy.evaluate(humans, zombies);
                if (bestScore < score) {
                    bestScore = score;
                    bestStrategy = strategy;
                }
            }

            System.out.println(bestStrategy.getFirstStep());

        }
    }

    private static Strategy generateRandomStrategy(int id, int seed, List<Zombie> zombies) {
        Random random = new Random(seed);
        int movements = random.nextInt(3);
        List<String> movePoints = new ArrayList<>();
        Strategy strategy = new Strategy(id, movePoints);

        // move to random position
        for (int i = 0; i < movements; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            movePoints.add(x+" "+y);
        }

        List<Zombie> zombiesAlive = new ArrayList<>(zombies);
        Collections.shuffle(zombiesAlive, random);

        for(Zombie zombieAlive : zombiesAlive) {
            // move to the zombie until it is dead
            int[] lastPos = strategy.getLastPoint();
            while(zombieAlive.squareDistanceFrom(lastPos[0], lastPos[1]) > ASH_RADIUS_SQ) {
                int x = zombieAlive.x;
                int y = zombieAlive.y;
                movePoints.add(moveAshTowards(lastPos, new int[]{x, y}));
            }
        }

        return strategy;
    }

    private static String moveAshTowards(int[] ashPos, int[] targetPos) {
        int dx = targetPos[0] - ashPos[0];
        int dy = targetPos[1] - ashPos[1];
        double angle = Math.atan2(dy, dx);
        int x = (int) (ashPos[0] + Math.cos(angle) * 1000);
        int y = (int) (ashPos[1] + Math.sin(angle) * 1000);
        return x+" "+y;
    }

    static class Strategy {
        private final int id;

        private final List<String> movements;

        public Strategy(int id, List<String> movements) {
            this.id = id;
            this.movements = movements;
        }

        public List<String> getMovements() {
            return movements;
        }

        public String getFirstStep() {
            return movements.get(0);
        }

        public int[] getLastPoint() {
            String p = movements.get(movements.size()-1);
            String[] split = p.split(" ");
            return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])};
        }

        public int evaluate(List<Human> humans, List<Zombie> zombies) {
            // todo
            return -1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Strategy strategy = (Strategy) o;
            return id == strategy.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
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