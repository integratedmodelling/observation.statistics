package org.operations;

import java.util.ArrayList;

import static java.lang.Math.*;

public class RealValuedOperands {

    public static double difference(double referenceValue, double otherValue, boolean relative, boolean signed){
        double ret;

        if(signed){
            ret = (referenceValue - otherValue);
            if (relative){
                ret /= referenceValue;
            }
        } else{
            ret = abs(referenceValue - otherValue);
            if (relative){
                ret /= abs(referenceValue);
            }
        }

        return ret;

    }

    public static double mean(ArrayList<Number> values){

        return values.stream()
                .mapToDouble(a -> (double) a)
                .average()
                .orElse( Double.NaN );

    }

    public static double variance(ArrayList<Number> values, Double mean){

        return values.stream()
                .mapToDouble(a ->  pow(((double) a - mean),2))
                .average()
                .orElse( Double.NaN );

    }

    public static double covariance(ArrayList<Number> referenceValues, ArrayList<Number> otherValues, Double referenceMean, Double otherMean){

        if (referenceValues.size() != otherValues.size()) {
            throw new ArithmeticException();
        } else {
            ArrayList<Double> product = new ArrayList<>();
            for (int i = 0; i < referenceValues.size(); i++) {
                product.add(((double) referenceValues.get(i) - referenceMean) * ((double) otherValues.get(i) - otherMean));
            }
            return product.stream().mapToDouble(a->a).average().orElse(Double.NaN);
        }

    }


    public static double similarityInMean(Double referenceWeightedMean, Double otherWeightedMean, Double c1){

        return ( 2 * referenceWeightedMean * otherWeightedMean + c1) / ( pow(referenceWeightedMean,2) + pow(otherWeightedMean,2) +c1 );

    }

    public static double similarityInVariance(Double referenceWeightedVariance, Double otherWeightedVariance, Double c2){

        return ( 2 * sqrt(referenceWeightedVariance) * sqrt(otherWeightedVariance) + c2) / ( referenceWeightedVariance + otherWeightedVariance +c2 );

    }

    public static double similarityInPattern(Double covariance,Double referenceWeightedVariance, Double otherWeightedVariance, Double c3){

        return (covariance + c3)/(sqrt(referenceWeightedVariance) * sqrt(otherWeightedVariance) + c3);

    }

    public static double structuralSimilarity(Double sim, Double siv, Double sip, Double relevanceMean, Double relevanceVariance, Double relevancePattern){
        return pow(sim,relevanceMean)*pow(siv,relevanceVariance)*pow(sip,relevancePattern);
    }

}
