package ch.codingame.codevszombies.export;

import ch.codingame.codevszombies.GameEngine;
import ch.codingame.codevszombies.GameState;
import ch.codingame.codevszombies.GameplayRecorder;
import ch.codingame.codevszombies.Position;

public class SvgExporter {

    public static final String BG_FILL = "#FFFFFF";

    public static final String HUMAN_FILL = "#00FF00";

    public static final String ZOMBIE_FILL = "#000000";

    public static final String ASH_FILL = "#ebd426";

    public static final int HUMAN_RAD = 50;
    public static final int ZOMBIE_RAD = 50;

    public static final int ASH_RAD = 50;

    public static final int ASH_STROKE_WIDTH = 10;

    public static String exportToSvg(GameState initialState, GameplayRecorder gameplay, int width, int height) {
        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width).append("\" height=\"").append(height).append("\" viewBox=\"0 0").append(width).append(" ").append(height).append("\">\n");
        sb.append("<g transform=\"scale(0.25 0.25)\">\n");
        sb.append("<rect width=\"").append(width).append("\" height=\"").append(height).append("\" fill=\""+BG_FILL+"\" stroke=\"#000000\" stroke-width=\"5\"/>\n");

        // humans
        for (Position human : initialState.getHumans()) {
            sb.append("<circle cx=\"").append(human.x()).append("\" cy=\"").append(human.y()).append("\" r=\"").append(HUMAN_RAD).append("\" fill=\""+HUMAN_FILL+"\" />\n");
        }

        // zombies
        for (Position zombie : initialState.getZombies()) {
            // range
            sb.append("<circle cx=\"").append(zombie.x()).append("\" cy=\"").append(zombie.y()).append("\" r=\"").append(GameEngine.ZOMBIE_RANGE).append("\" fill=\""+ZOMBIE_FILL+"\" opacity=\"0.3\"/>\n");

            // zombie
            sb.append("<circle cx=\"").append(zombie.x()).append("\" cy=\"").append(zombie.y()).append("\" r=\"").append(ZOMBIE_RAD).append("\" fill=\""+ZOMBIE_FILL+"\" />\n");
        }

        // ash's movement, initial position is included
        Position lastPos = null;
        for (Position ashPos : gameplay.getAshMovement()) {
            // ash's range
            sb.append("<circle cx=\"").append(ashPos.x()).append("\" cy=\"").append(ashPos.y()).append("\" r=\"").append(GameEngine.ASH_RANGE).append("\" fill=\""+ASH_FILL+"\" opacity=\"0.1\"/>\n");
            // ash
            sb.append("<circle cx=\"").append(ashPos.x()).append("\" cy=\"").append(ashPos.y()).append("\" r=\"").append(ASH_RAD).append("\" fill=\""+ASH_FILL+"\" />\n");

            if (lastPos != null) {
                sb.append("<line x1=\"").append(lastPos.x()).append("\" y1=\"").append(lastPos.y()).append("\" x2=\"").append(ashPos.x()).append("\" y2=\"").append(ashPos.y()).append("\" stroke=\""+ASH_FILL+"\" stroke-width=\""+ASH_STROKE_WIDTH+"\" />\n");
            }

            lastPos = ashPos;
        }

        sb.append("</g>\n");
        sb.append("</svg>");
        return sb.toString();
    }
}
