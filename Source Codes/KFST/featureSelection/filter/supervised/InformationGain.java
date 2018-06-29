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
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package KFST.featureSelection.filter.supervised;

import KFST.util.ArraysFunc;
import KFST.util.MathFunc;
import java.util.Arrays;
import KFST.featureSelection.filter.WeightedFilterApproach;

/**
 * This java class is used to implement the information gain method.
 *
 * @author Sina Tabakhi
 * @see KFST.featureSelection.filter.WeightedFilterApproach
 * @see KFST.featureSelection.FeatureWeighting
 * @see KFST.featureSelection.FeatureSelection
 */
public class InformationGain extends WeightedFilterApproach {

    /**
     * initializes the parameters
     *
     * @param arguments array of parameters contains 
     * (<code>sizeSelectedFeatureSubset</code>) in which 
     * <code><b><i>sizeSelectedFeatureSubset</i></b></code> is the number of 
     * selected features
     */
    public InformationGain(Object... arguments) {
        super((int)arguments[0]);
    }
    
    /**
     * initializes the parameters
     *
     * @param sizeSelectedFeatureSubset the number of selected features
     */
    public InformationGain(int sizeSelectedFeatureSubset) {
        super(sizeSelectedFeatureSubset);
    }

    /**
     * computes the entropy of the data given by start and end indices
     *
     * @param indexStart the start index of the dataset
     * @param indexEnd the end index of the dataset
     * 
     * @return the entropy value
     */
    private double computeEntropy(int indexStart, int indexEnd) {
        double entropy = 0;
        int sizeUsedData = indexEnd - indexStart;
        int[] countClassSample = new int[numClass];

        //counts the number of samples in each class
        for (int i = indexStart; i < indexEnd; i++) {
            countClassSample[(int) trainSet[i][numFeatures]]++;
        }

        //computes the probability of each class
        for (int i = 0; i < numClass; i++) {
            if (countClassSample[i] != 0) {
                double prob = countClassSample[i] / (double) sizeUsedData;
                entropy -= prob * MathFunc.log2(prob);
            }
        }

        return entropy;
    }

    /**
     * starts the feature selection process by information gain(IG) method
     */
    @Override
    public void evaluateFeatures() {
        double entropySystem = computeEntropy(0, trainSet.length); // computes the entropy of the system (over all dataset)
        featureValues = new double[numFeatures];
        int[] indecesIG;

        //computes the information gain values of each feature
        for (int i = 0; i < numFeatures; i++) {
            double entropyFeature = 0;
            ArraysFunc.sortArray2D(trainSet, i); // sorts the dataset values corresponding to a given feature(feature i)
            int indexStart = 0;
            double startValue = trainSet[indexStart][i];
            for (int j = 1; j < trainSet.length; j++) {
                if (startValue != trainSet[j][i]) {
                    double prob = (j - indexStart) / (double) trainSet.length;
                    entropyFeature += prob * computeEntropy(indexStart, j);
                    indexStart = j;
                    startValue = trainSet[indexStart][i];
                }
            }
            double prob = (trainSet.length - indexStart) / (double) trainSet.length;
            entropyFeature += prob * computeEntropy(indexStart, trainSet.length);
            featureValues[i] = entropySystem - entropyFeature;
        }

        indecesIG = ArraysFunc.sortWithIndex(Arrays.copyOf(featureValues, featureValues.length), true);
//        for (int i = 0; i < numFeatures; i++) {
//            System.out.println(i + ") =  " + featureValues[i]);
//        }

        selectedFeatureSubset = Arrays.copyOfRange(indecesIG, 0, numSelectedFeature);
        ArraysFunc.sortArray1D(selectedFeatureSubset, false);
//        for (int i = 0; i < numSelectedFeature; i++) {
//            System.out.println("ranked  = " + selectedFeatureSubset[i]);
//        }
    }
}
