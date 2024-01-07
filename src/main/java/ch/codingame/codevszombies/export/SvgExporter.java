package ch.codingame.codevszombies.export;

import ch.codingame.codevszombies.GameEngine;
import ch.codingame.codevszombies.GameState;
import ch.codingame.codevszombies.GameplayRecorder;
import ch.codingame.codevszombies.Position;

import java.util.List;

public class SvgExporter {

    public static final String BG_FILL = "#FFFFFF";

    public static final String HUMAN_FILL = "#00FF00";

    public static final String ZOMBIE_FILL = "#000000";

    public static final String ASH_FILL = "#ebd426";

    public static final int HUMAN_RAD = 50;
    public static final int ZOMBIE_RAD = 50;

    public static final int ASH_RAD = 50;

    public static final int ASH_STROKE_WIDTH = 10;

    public static String exportToSvg(GameState initialState, GameplayRecorder gameplay, int width, int height, int desiredWidth, int desiredHeight, int score) {
        StringBuilder sb = new StringBuilder();
        final double scaleX = desiredWidth/(double)width;
        final double scaleY = desiredHeight/(double)height;
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(desiredWidth).append("\" height=\"").append(desiredHeight+150).append("\" viewBox=\"0 0").append(desiredWidth).append(" ").append(desiredHeight+150).append("\">\n");
        sb.append("<style>\n" +
                ".heavy {\n" +
                "      font: bold 40px sans-serif;\n" +
                "      fill: #000000;\n" +
                "    }\n" +
                "</style>\n"
        );
        sb.append("<defs>\n" +
                "    <marker \n" +
                "      id='head-z' \n" +
                "      orient=\"auto\" \n" +
                "      markerWidth='5' \n" +
                "      markerHeight='10' \n" +
                "      refX='10' \n" +
                "      refY='5'\n" +
                "    >\n" +
                "      <path d='M0,0 V10 L5,5 Z' fill=\""+ZOMBIE_FILL+"\" />\n" +
                "    </marker>\n" +
                "    <marker \n" +
                "      id='head-a' \n" +
                "      orient=\"auto\" \n" +
                "      markerWidth='5' \n" +
                "      markerHeight='10' \n" +
                "      refX='10' \n" +
                "      refY='5'\n" +
                "    >\n" +
                "      <path d='M0,0 V10 L5,5 Z' fill=\""+ASH_FILL+"\" />\n" +
                "    </marker>\n" +
                "  </defs>");

        sb.append("<g transform=\"scale("+scaleX+" "+scaleY+")\">\n");
        sb.append("<rect width=\"").append(width).append("\" height=\"").append(height).append("\" fill=\""+BG_FILL+"\" stroke=\"#000000\" stroke-width=\"5\"/>\n");
        // zombies
        for (int zombieId = 0; zombieId < gameplay.getZombiesMovement().size(); zombieId++) {
            List<Position> zombieMovement = gameplay.getZombiesMovement().get(zombieId);
            Position lastPos = null;
            for (Position zombiePos : zombieMovement) {
                // zombie's range
                sb.append("<circle cx=\"").append(zombiePos.x()).append("\" cy=\"").append(zombiePos.y()).append("\" r=\"").append(GameEngine.ZOMBIE_RANGE).append("\" fill=\""+ZOMBIE_FILL+"\" opacity=\"0.1\"/>\n");
                // zombie
                sb.append("<circle cx=\"").append(zombiePos.x()).append("\" cy=\"").append(zombiePos.y()).append("\" r=\"").append(ZOMBIE_RAD).append("\" fill=\""+ZOMBIE_FILL+"\" />\n");

                if (lastPos != null && !zombiePos.equals(lastPos)) {
                    final String d = String.format("M%d,%d L%d,%d", lastPos.x(), lastPos.y(), zombiePos.x(), zombiePos.y());
                    sb.append("<path marker-end=\"url(#head-z)\" d=\"").append(d).append("\" stroke=\"").append(ZOMBIE_FILL).append("\" stroke-width=\"").append(ASH_STROKE_WIDTH).append("\" />\n");
                }

                lastPos = zombiePos;
            }
        }

        // ash's movement, initial position is included
        Position lastPos = null;
        for (Position ashPos : gameplay.getAshMovement()) {
            // ash's range
            sb.append("<circle cx=\"").append(ashPos.x()).append("\" cy=\"").append(ashPos.y()).append("\" r=\"").append(GameEngine.ASH_RANGE).append("\" stroke=\""+ASH_FILL+"\"").append(" stroke-width=\"").append(ASH_STROKE_WIDTH).append("\" fill=\"none\" opacity=\"0.5\"/>\n");
            // ash
            sb.append("<circle cx=\"").append(ashPos.x()).append("\" cy=\"").append(ashPos.y()).append("\" r=\"").append(ASH_RAD).append("\" fill=\""+ASH_FILL+"\" />\n");

            if (lastPos != null) {
                final String d = String.format("M%d,%d L%d,%d", lastPos.x(), lastPos.y(), ashPos.x(), ashPos.y());
                sb.append("<path marker-end=\"url(#head-a)\" d=\"").append(d).append("\" stroke=\"").append(ASH_FILL).append("\" stroke-width=\"").append(ASH_STROKE_WIDTH).append("\" />\n");
            }

            lastPos = ashPos;
        }

        // humans
        for (Position human : gameplay.getRemainingHumans()) {
            sb.append("<circle cx=\"").append(human.x()).append("\" cy=\"").append(human.y()).append("\" r=\"").append(HUMAN_RAD).append("\" fill=\""+HUMAN_FILL+"\" />\n");
        }

        sb.append("</g>\n");

        sb.append("<text x=\"10\" y=\""+(height*scaleY + 50)+"\" class=\"heavy\">Score: ").append(score).append("</text>\n");
        sb.append("</svg>");
        return sb.toString();
    }
}
