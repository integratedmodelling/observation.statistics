package org.operations;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.jaitools.media.jai.kernel.KernelFactory;

import javax.media.jai.KernelJAI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.WritableRandomIter;
import java.awt.image.WritableRaster;

import static java.lang.Math.pow;
import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;
import static org.operations.RealValuedOperands.difference;

public class Convolution {


    public static GridCoverage2D weightedMean(GridCoverage2D map, int radius, KernelFactory.ValueType type){

        RandomIter inputIter = CoverageUtilities.getRandomIterator(map);

        double nv= HMConstants.getNovalue(map);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(map);

        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster outWR = CoverageUtilities.createWritableRaster(cols, rows, null, null, nv);
        WritableRandomIter outIter = CoverageUtilities.getWritableRandomIterator(outWR);

        KernelJAI kernel = KernelFactory.createCircle(radius, type);
        float [] kernelData = kernel.getKernelData();

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {

                    double inputValue = inputIter.getSampleDouble(c, r, 0);
                    if (HMConstants.isNovalue(inputValue, nv)) {
                        continue;
                    }

                    int k = 0;
                    double mean = 0;
                    for( int kr = -radius; kr <= radius; kr++) {
                        for( int kc = -radius; kc <= radius; kc++, k++ ) {
                            double value = inputIter.getSampleDouble(c + kc, r + kr, 0);
                            if (HMConstants.isNovalue(value, nv)) {
                                value = 0;
                            }
                            mean += kernelData[k] * value;
                        }
                    }
                    outIter.setSample(c, r, 0, mean);

                }
            }
        } finally {
            inputIter.done();
            outIter.done();
        }

        return CoverageUtilities.buildCoverage("weightedmean", outWR, regionMap, map.getCoordinateReferenceSystem());

    }


    public static GridCoverage2D weightedVariance(GridCoverage2D map, GridCoverage2D meanMap, int radius, KernelFactory.ValueType type){

        RandomIter inputIter = CoverageUtilities.getRandomIterator(map);
        RandomIter meanIter = CoverageUtilities.getRandomIterator(meanMap);

        double nv= HMConstants.getNovalue(map);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(map);

        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster outWR = CoverageUtilities.createWritableRaster(cols, rows, null, null, nv);
        WritableRandomIter outIter = CoverageUtilities.getWritableRandomIterator(outWR);

        KernelJAI kernel = KernelFactory.createCircle(radius, type);
        float [] kernelData = kernel.getKernelData();

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {

                    double meanValue = meanIter.getSampleDouble(c,r,0);

                    int k = 0;
                    double variance = 0;
                    for( int kr = -radius; kr <= radius; kr++) {
                        for( int kc = -radius; kc <= radius; kc++, k++ ) {
                            double value = inputIter.getSampleDouble(c + kc, r + kr, 0);
                            if (HMConstants.isNovalue(value, nv)) {
                                value = meanValue;
                            }
                            variance += kernelData[k] * pow((value-meanValue),2);
                        }
                    }
                    outIter.setSample(c, r, 0, variance);

                }
            }
        } finally {
            inputIter.done();
            outIter.done();
            meanIter.done();
        }

        return CoverageUtilities.buildCoverage("weightedvariance", outWR, regionMap, map.getCoordinateReferenceSystem());

    }


    public static GridCoverage2D weightedCovariance(GridCoverage2D map1, GridCoverage2D meanMap1, GridCoverage2D map2, GridCoverage2D meanMap2, int radius, KernelFactory.ValueType type){

        RandomIter inputIter1 = CoverageUtilities.getRandomIterator(map1);
        RandomIter meanIter1 = CoverageUtilities.getRandomIterator(meanMap1);
        RandomIter inputIter2 = CoverageUtilities.getRandomIterator(map2);
        RandomIter meanIter2 = CoverageUtilities.getRandomIterator(meanMap2);

        double nv1= HMConstants.getNovalue(map1);
        double nv2= HMConstants.getNovalue(map2);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(map1);
        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster outWR = CoverageUtilities.createWritableRaster(cols, rows, null, null, nv1);
        WritableRandomIter outIter = CoverageUtilities.getWritableRandomIterator(outWR);

        KernelJAI kernel = KernelFactory.createCircle(radius, type);
        float [] kernelData = kernel.getKernelData();

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {

                    double meanValue1 = meanIter1.getSampleDouble(c,r,0);
                    double meanValue2 = meanIter1.getSampleDouble(c,r,0);

                    int k = 0;
                    double covariance = 0;
                    for( int kr = -radius; kr <= radius; kr++) {
                        for( int kc = -radius; kc <= radius; kc++, k++ ) {
                            double value1 = inputIter1.getSampleDouble(c + kc, r + kr, 0);
                            double value2 = inputIter2.getSampleDouble(c + kc, r + kr, 0);
                            if (HMConstants.isNovalue(value1, nv1) || HMConstants.isNovalue(value2, nv2)) {
                                value1 = meanValue1;
                                value2 = meanValue2;
                            }
                            covariance += kernelData[k] * (value1-meanValue1) * (value2-meanValue2);
                        }
                    }
                    outIter.setSample(c, r, 0, covariance);

                }
            }
        } finally {
            inputIter1.done();
            inputIter2.done();
            outIter.done();
            meanIter1.done();
            meanIter2.done();
        }

        return CoverageUtilities.buildCoverage("weightedvariance", outWR, regionMap, map1.getCoordinateReferenceSystem());

    }



}
