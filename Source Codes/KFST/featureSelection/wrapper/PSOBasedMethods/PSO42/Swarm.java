/*
 * Kurdistan Feature Selection Tool (KFST) is an open-source tool, developed
 * completely in Java, for performing feature selection process in different
 * areas of research.
 * For more information about KFST, please visit:
 *     http://kfst.uok.ac.ir/index.html
 *
 * Copyright (C) 2016-2018 KFST development team at University of Kurdistan,
 * Sanandaj, Iran.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package KFST.featureSelection.wrapper.PSOBasedMethods.PSO42;

import KFST.featureSelection.wrapper.PSOBasedMethods.BasicSwarm;
import KFST.result.performanceMeasure.Criteria;
import KFST.util.MathFunc;
import java.util.Arrays;
import java.util.Random;

/**
 * This java class is used to implement a swarm of particles in particle swarm
 * optimization version 4-2(PSO(4-2)) method in which the type of position
 * vector is double and the type of particle is extended from Particle class.
 *
 * @author Sina Tabakhi
 * @see KFST.featureSelection.wrapper.PSOBasedMethods.BasicSwarm
 */
public class Swarm extends BasicSwarm<Double, Particle> {

    private int seedValue = 0;
    private Random rand = new Random(seedValue);

    /**
     * initializes the parameters
     *
     * @param theta the threshold is used to determine whether a feature is
     * selected or not
     */
    public Swarm(double theta) {
        super(Double.class, Particle.class);
        Particle.THETA = theta;
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = new Particle(PROBLEM_DIMENSION);
        }
    }

    /**
     * {@inheritDoc }
     * Particles are initialized based on small initialization and large
     * initialization strategies. Most of the particles are initialized using
     * small feature subsets (2/3 of the population) and the other particles are
     * initialized using relatively large feature subsets (1/3 of the
     * population).
     */
    @Override
    public void initialization() {
        int numSmallParticles = (2 * POPULATION_SIZE) / 3;

        /**
         * Initialized particle's position by small strategy (2/3 of the
         * population). 10% of the total number of features will be selected for
         * each particle
         */
        int numSelectedFeats = (int) (0.1 * PROBLEM_DIMENSION);
        for (int par = 0; par < numSmallParticles; par++) {
            population[par].position = generateRandomFeatureSubset(numSelectedFeats);
        }

        /**
         * Initialized particle's position by large strategy (1/3 of the
         * population). The number of selected features will be chosen by a
         * random number which is generated in a range [POPULATION_SIZE / 2,
         * POPULATION_SIZE]
         */
        for (int par = numSmallParticles; par < POPULATION_SIZE; par++) {
            numSelectedFeats = MathFunc.generateRandNum(PROBLEM_DIMENSION / 2, PROBLEM_DIMENSION, rand);
            population[par].position = generateRandomFeatureSubset(numSelectedFeats);
        }

        //Initialized particle's velocity randomly
        for (int par = 0; par < POPULATION_SIZE; par++) {
            for (int dim = 0; dim < PROBLEM_DIMENSION; dim++) {
                population[par].velocity[dim] = MathFunc.generateRandNum(MIN_VELOCITY, MAX_VELOCITY, rand);
            }
        }
    }

    /**
     * {@inheritDoc }
     * K-fold cross validation on training set is used for evaluating the
     * classification performance of selected feature subset by each particle.
     */
    @Override
    public void evaluateFitness() {
        for (int par = 0; par < POPULATION_SIZE; par++) {
            if (population[par].numSelectedFeatures() > 0) {
                Criteria criteria = fitnessEvaluator.crossValidation(population[par].selectedFeaturesSubset());
                population[par].setFitness(criteria.getAccuracy());
            } else {
                population[par].setFitness(0);
            }
        }
    }

    /**
     * {@inheritDoc }
     * Personal best position is updated when the classification performance of
     * the particle's new position is better than personal best. Also, if the
     * classification performance of the particle's new position is same as
     * personal best and the number of selected features is smaller, the
     * personal best will be updated and replaced by particle's new position.
     */
    @Override
    public void updatePersonalBest() {
        for (int par = 0; par < POPULATION_SIZE; par++) {
            if ((population[par].getFitness() > population[par].getPBestFitness())
                    || ((population[par].getFitness() == population[par].getPBestFitness())
                    && (population[par].numSelectedFeatures() < Particle.numSelectedFeatures(population[par].pBest)))) {
                population[par].pBest = Arrays.copyOf(population[par].position, PROBLEM_DIMENSION);
                population[par].setPBestFitness(population[par].getFitness());
            }
        }
    }

    /**
     * {@inheritDoc }
     * Global best position is updated when the classification performance of
     * any personal best position of the particles is better than global best.
     * Also, if the classification performance of the any personal best position
     * is same as global best and the number of selected features is smaller,
     * the global best will be updated and replaced by personal best position.
     */
    @Override
    public void updateGlobalBest() {
        int maxIndex = 0;
        double maxValue = population[0].getPBestFitness();
        int numFeatSubset = Particle.numSelectedFeatures(population[0].pBest);
        for (int par = 1; par < POPULATION_SIZE; par++) {
            if ((population[par].getPBestFitness() > maxValue)
                    || ((population[par].getPBestFitness() == maxValue)
                    && (Particle.numSelectedFeatures(population[par].pBest) < numFeatSubset))) {
                maxIndex = par;
                maxValue = population[par].getPBestFitness();
                numFeatSubset = Particle.numSelectedFeatures(population[par].pBest);
            }
        }

        if ((maxValue > this.getGBestFitness())
                || ((maxValue == this.getGBestFitness())
                && (numFeatSubset < Particle.numSelectedFeatures(this.gBest)))) {
            this.setGBest(Arrays.copyOf(population[maxIndex].pBest, PROBLEM_DIMENSION));
            this.setGBestFitness(maxValue);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateParticleVelocity() {
        for (int par = 0; par < POPULATION_SIZE; par++) {
            for (int dim = 0; dim < PROBLEM_DIMENSION; dim++) {
                double firstPart = INERTIA_WEIGHT * population[par].velocity[dim];
                double secondPart = C1 * rand.nextDouble() * (population[par].pBest[dim] - population[par].position[dim]);
                double thirdPart = C2 * rand.nextDouble() * (gBest[dim] - population[par].position[dim]);
                population[par].velocity[dim] = firstPart + secondPart + thirdPart;
                if (population[par].velocity[dim] > MAX_VELOCITY) {
                    population[par].velocity[dim] = MAX_VELOCITY;
                } else if (population[par].velocity[dim] < MIN_VELOCITY) {
                    population[par].velocity[dim] = MIN_VELOCITY;
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateParticlePosition() {
        for (int par = 0; par < POPULATION_SIZE; par++) {
            for (int dim = 0; dim < PROBLEM_DIMENSION; dim++) {
                population[par].position[dim] += population[par].velocity[dim];
                if (population[par].position[dim] > END_POS_INTERVAL) {
                    population[par].position[dim] = END_POS_INTERVAL;
                } else if (population[par].position[dim] < START_POS_INTERVAL) {
                    population[par].position[dim] = START_POS_INTERVAL;
                }
            }
        }
    }

    /**
     * This method generates a position for a particle in which the number of
     * selected features is defined by input value.
     *
     * @param numFeatures the number of selected features for particle
     *
     * @return a generated position for a particle
     */
    private Double[] generateRandomFeatureSubset(int numFeatures) {
        Double[] particlePositionState = new Double[PROBLEM_DIMENSION];

        /**
         * values of selected features. Generates a random number in a range
         * (theta, END_POS_INTERVAL]
         */
        for (int dim = 0; dim < numFeatures; dim++) {
            particlePositionState[dim] = MathFunc.generateRandNum(Particle.THETA + 0.001, END_POS_INTERVAL, rand);
        }

        /**
         * values of not selected features. Generates a random number in a range
         * [START_POS_INTERVAL, theta]
         */
        for (int dim = numFeatures; dim < PROBLEM_DIMENSION; dim++) {
            particlePositionState[dim] = MathFunc.generateRandNum(START_POS_INTERVAL, Particle.THETA, rand);
        }

        MathFunc.randomize(particlePositionState);

        return particlePositionState;
    }
}
