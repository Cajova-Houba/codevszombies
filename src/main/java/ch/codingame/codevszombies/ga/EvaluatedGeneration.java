package ch.codingame.codevszombies.ga;

public class EvaluatedGeneration {

    public final EvaluatedChromosome[] generation;

    public final int bestId;
    public final int bestScore;

    public final int worstScore;

    public final int meanScore;

    public final int averageScore;

    public EvaluatedGeneration(EvaluatedChromosome[] generation, int bestId, int bestScore, int worstScore, int meanScore, int averageScore) {
        this.generation = generation;
        this.bestId = bestId;
        this.bestScore = bestScore;
        this.worstScore = worstScore;
        this.meanScore = meanScore;
        this.averageScore = averageScore;
    }
}
