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
package KFST.featureSelection.wrapper.ACOBasedMethods;

import KFST.classifier.ClassifierType;
import KFST.featureSelection.FitnessEvaluator;
import KFST.featureSelection.wrapper.WrapperApproach;

/**
 * The abstract class contains the main methods and fields that are used in all
 * ACO-based feature selection methods.
 *
 * @param <ColonyType> the type of colony implemented in ACO algorithm
 *
 * @author Sina Tabakhi
 * @see KFST.featureSelection.wrapper.WrapperApproach
 * @see KFST.featureSelection.FeatureSelection
 */
public abstract class BasicACO<ColonyType> extends WrapperApproach {

    protected ColonyType colony;
    protected final int NUM_ITERATION;
    protected final int K_FOLDS;

    /**
     * Initializes the parameters
     *
     * @param arguments array of parameters contains (<code>path</code>,
     * <code>numFeatures</code>, <code>classifierType</code>,
     * <code>selectedClassifierPan</code>, <code>numIteration</code>,
     * <code>colonySize</code>, <code>alphaParameter</code>,
     * <code>betaParameter</code>, <code>evaporationRate</code>,
     * <code>kFolds</code>, <code>initPheromone</code>) in which
     * <code><b><i>path</i></b></code> is the path of the project,
     * <code><b><i>numFeatures</i></b></code> is the number of original features
     * in the dataset, <code><b><i>classifierType</i></b></code> is the
     * classifier type for evaluating the fitness of a solution,
     * <code><b><i>selectedClassifierPan</i></b></code> is the selected
     * classifier panel, <code><b><i>numIteration</i></b></code> is the maximum
     * number of allowed iterations that algorithm repeated,
     * <code><b><i>colonySize</i></b></code> is the size of colony of candidate
     * solutions, <code><b><i>alphaParameter</i></b></code> is the alpha
     * parameter used in the state transition rule that shows the relative
     * importance of the pheromone, <code><b><i>betaParameter</i></b></code> is
     * the beta parameter used in the state transition rule that shows the
     * relative importance of heuristic information,
     * <code><b><i>evaporationRate</i></b></code> is the evaporation rate of the
     * pheromone, <code><b><i>kFolds</i></b></code> is the number of equal sized
     * subsamples that is used in k-fold cross validation, and
     * <code><b><i>initPheromone</i></b></code> is the initial value of the
     * pheromone
     */
    public BasicACO(Object... arguments) {
        super((String) arguments[0]);
        BasicColony.NUM_ORIGINAL_FEATURE = (int) arguments[1];
        this.NUM_ITERATION = (int) arguments[4];
        BasicColony.COLONY_SIZE = (int) arguments[5];
        BasicColony.ALPHA = (double) arguments[6];
        BasicColony.BETA = (double) arguments[7];
        BasicColony.RHO = (double) arguments[8];
        this.K_FOLDS = (int) arguments[9];
        BasicColony.INIT_PHEROMONE_VALUE = (double) arguments[10];

        BasicColony.fitnessEvaluator = new FitnessEvaluator(TEMP_PATH, (ClassifierType) arguments[2],
                arguments[3], this.K_FOLDS);
    }

    /**
     * This method creates the selected feature subset based on the best ant in
     * the colony.
     *
     * @return the array of indices of the selected feature subset
     */
    protected abstract int[] createSelectedFeatureSubset();
}
