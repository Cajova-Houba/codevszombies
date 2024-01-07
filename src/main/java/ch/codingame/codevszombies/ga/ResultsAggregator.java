package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.GameplayRecorder;

import java.util.ArrayList;
import java.util.List;

public class ResultsAggregator {

    private final List<EvaluatedGeneration> generations;
    private final List<GameplayRecorder[]> generationGameplays;
    public ResultsAggregator() {
        generations = new ArrayList<>();
        generationGameplays = new ArrayList<>();
    }

    public void addGeneration(EvaluatedChromosome[] generation, GameplayRecorder[] gameplays) {
        generations.add(evaluateGeneration(generation));
        generationGameplays.add(gameplays);
    }

    public List<GameplayRecorder[]> getGenerationGameplays() {
        return generationGameplays;
    }

    /**
     * [0] = id of the generation
     * [1] = id of the chromosome in the generation
     */
    public int[] getBest() {
        int bestId = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < generations.size(); i++) {
            EvaluatedGeneration generation = generations.get(i);
            if (generation.bestScore > bestScore) {
                bestId = i;
                bestScore = generation.bestScore;
            }
        }
        return new int[] {bestId, generations.get(bestId).bestId};
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
