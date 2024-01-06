package ch.codingame.codevszombies.ga;

/**
 *
 * @param populationSize
 * @param generations
 * @param matingCount Must be <= populationSize and > 0.
 * @param matingCoefficient Must be between 0 and 1. Used when mating two genes.
 */
public record AlgorithmConfiguration(int populationSize, int generations, int matingCount, float matingCoefficient, float mutationRate) {
}
