package ch.codingame.codevszombies.ga;

import ch.codingame.codevszombies.Position;

public record EvaluatedChromosome(ChromosomeSolution chromosome, int score) {

    public Position getNthGene(int n) {
        return chromosome.getMoves()[n];
    }

    /**
     * Mating formula for both coordinates:
     *  c1 = p1 - beta*(p1 - p2).
     *  c2 = p2 + beta*(p1 - p2).
     *
     * Since both p1 and p2 are expected to be >=0, both children will be same
     *
     * @param other Chromosome to mate with.
     * @param matingCoefficient Must be between 0 and 1. Used when mating two genes.
     * @return Two child chromosomes.
     */
    public ChromosomeSolution[]     mateWith(EvaluatedChromosome other, float matingCoefficient) {
        Position[] genes1 = new Position[chromosome.getMoves().length];
        Position[] genes2 = new Position[chromosome.getMoves().length];
        for (int i = 0; i < chromosome.getMoves().length; i++) {
            int p1x = this.getNthGene(i).x();
            int p2x = other.getNthGene(i).x();
            int p1y = this.getNthGene(i).y();
            int p2y = other.getNthGene(i).y();

            genes1[i] = new Position(
                    (int)(p1x - matingCoefficient * (p1x - p2x) ),
                    (int)(p1y - matingCoefficient * (p1y - p2y) )
            );

            genes2[i] = new Position(
                    (int)(p2x + matingCoefficient * (p1x - p2x) ),
                    (int)(p2y + matingCoefficient * (p1y - p2y) )
            );
        }
        return new ChromosomeSolution[]{ new ChromosomeSolution(genes1), new ChromosomeSolution(genes2) };
    }
}
