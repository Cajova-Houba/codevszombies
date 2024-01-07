package ch.codingame.codevszombies.ga;

/**
 *
 * @param populationSize
 * @param generations
 * @param matingCount Must be <= populationSize and > 0.
 * @param crossoverPoint Must be > 0 and < chromosome length.
 *                       0 = all genes are created by parents mating. chromosomeSize = genes are kept from parents.
 *                       For example if set to 10, then the first 10 genes are inherited from parent, the rest is obtained
 *                       by mating.
 * @param matingCoefficient Must be between 0 and 1. Used when mating two genes.
 */
public record AlgorithmConfiguration(int populationSize, int generations, int matingCount, int crossoverPoint, float matingCoefficient, float mutationRate) {
}
