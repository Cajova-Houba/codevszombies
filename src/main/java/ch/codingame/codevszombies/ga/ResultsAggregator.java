package ch.codingame.codevszombies.ga;

import java.util.ArrayList;
import java.util.List;

public class ResultsAggregator {

    private final List<EvaluatedGeneration> generations;
    public ResultsAggregator() {
        generations = new ArrayList<>();
    }

    public void addGeneration(EvaluatedChromosome[] generation) {
        generations.add(evaluateGeneration(generation));
    }

    public int getBestScore() {
        return generations.stream().mapToInt(g -> g.bestScore).max().orElse(-1);
    }

    public int[] getBestIds() {
        return generations.stream().mapToInt(g -> g.bestId).toArray();
    }

    public int[] getBestTrend() {
        return generations.stream().mapToInt(g -> g.bestScore).toArray();
    }

    public int[] getWorstTrend() {
        return generations.stream().mapToInt(g -> g.worstScore).toArray();
    }

    public int[] getMeanTrend() {
        return generations.stream().mapToInt(g -> g.meanScore).toArray();
    }

    public int[] getAverageTrend() {
        return generations.stream().mapToInt(g -> g.averageScore).toArray();
    }

    EvaluatedGeneration evaluateGeneration(EvaluatedChromosome[] generation) {
        int bestId = -1;
        int bestScore = Integer.MIN_VALUE;
        int worstScore = Integer.MAX_VALUE;
        int sum = 0;
        for (int i = 0; i < generation.length; i++) {
            EvaluatedChromosome chromosome = generation[i];
            int score = chromosome.score();
            if (score > bestScore) {
                bestScore = score;
                bestId = i;
            }
            if (score < worstScore) {
                worstScore = score;
            }
            sum += score;
        }
        int meanScore = sum / generation.length;
        int averageScore = (bestScore + worstScore) / 2;
        return new EvaluatedGeneration(generation, bestId, bestScore, worstScore, meanScore, averageScore);

    }
}
