package org.map.comparison;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;
import org.jaitools.media.jai.kernel.KernelFactory;
import org.operations.Convolution;
import org.operations.RealValuedOperands;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.WritableRandomIter;
import java.awt.image.WritableRaster;

import static org.operations.RealValuedOperands.difference;

public class StructuralSimilarity {

    public GridCoverage2D simMean;
    public GridCoverage2D simVariance;
    public GridCoverage2D simPattern;
    public GridCoverage2D simStructural;

    int radius;

    public GridCoverage2D getSimStructuralSimilarity() {
        return simStructural;
    }

    public StructuralSimilarity(GridCoverage2D referenceMap, GridCoverage2D otherMap, int radius){

        this.radius = radius;

        GridCoverage2D referenceConvolutedMean = Convolution.weightedMean(referenceMap,radius, KernelFactory.ValueType.GAUSSIAN);
        GridCoverage2D otherConvolutedMean = Convolution.weightedMean(otherMap,radius, KernelFactory.ValueType.GAUSSIAN);

        GridCoverage2D referenceConvolutedVariance = Convolution.weightedVariance(referenceMap,referenceConvolutedMean,radius, KernelFactory.ValueType.GAUSSIAN);
        GridCoverage2D otherConvolutedVariance = Convolution.weightedVariance(otherMap,otherConvolutedMean,radius, KernelFactory.ValueType.GAUSSIAN);

        GridCoverage2D convolutedCovariance = Convolution.weightedCovariance(referenceMap,referenceConvolutedMean,otherMap,otherConvolutedMean,radius, KernelFactory.ValueType.GAUSSIAN);

        double c1 = 1.0;
        double c2 = 2.0;
        double c3 = 3.0;

        this.simMean = calculateSimMean(referenceConvolutedMean,otherConvolutedMean,c1);
        this.simVariance = calculateSimVariance(referenceConvolutedVariance,otherConvolutedVariance,c2);
        this.simPattern = calculateSimPattern(convolutedCovariance,referenceConvolutedVariance,referenceConvolutedVariance,c3);

        this.simStructural = calculateSimStructural();

    }

    private static GridCoverage2D calculateSimMean(GridCoverage2D referenceMap, GridCoverage2D otherMap, double c1){

        RandomIter refIter = CoverageUtilities.getRandomIterator(referenceMap);
        RandomIter oIter = CoverageUtilities.getRandomIterator(otherMap);

        double refNV = HMConstants.getNovalue(referenceMap);
        double oNV = HMConstants.getNovalue(otherMap);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(referenceMap);
        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster outWR = CoverageUtilities.createWritableRaster(cols, rows, null, null, refNV);
        WritableRandomIter outIter = CoverageUtilities.getWritableRandomIterator(outWR);

        double sim;

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {
                    double refValue = refIter.getSampleDouble(c, r, 0);
                    double oValue = oIter.getSampleDouble(c,r,0);

                    sim = refNV;
                    if (!HMConstants.isNovalue(refValue, refNV) && !HMConstants.isNovalue(oValue, oNV)) {
                        sim = RealValuedOperands.similarityInMean(refValue,oValue,c1);
                    }
                    outIter.setSample(c, r, 0, sim);
                }
            }
        } finally {
            outIter.done();
            refIter.done();
            oIter.done();
        }

        return CoverageUtilities.buildCoverage("sim", outWR, regionMap, referenceMap.getCoordinateReferenceSystem());

    }

    private static GridCoverage2D calculateSimVariance(GridCoverage2D referenceMap, GridCoverage2D otherMap, double c2){

        RandomIter refIter = CoverageUtilities.getRandomIterator(referenceMap);
        RandomIter oIter = CoverageUtilities.getRandomIterator(otherMap);

        double refNV = HMConstants.getNovalue(referenceMap);
        double oNV = HMConstants.getNovalue(otherMap);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(referenceMap);
        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster outWR = CoverageUtilities.createWritableRaster(cols, rows, null, null, refNV);
        WritableRandomIter outIter = CoverageUtilities.getWritableRandomIterator(outWR);

        double siv;

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {
                    double refValue = refIter.getSampleDouble(c, r, 0);
                    double oValue = oIter.getSampleDouble(c,r,0);

                    siv = refNV;
                    if (!HMConstants.isNovalue(refValue, refNV) && !HMConstants.isNovalue(oValue, oNV)) {
                        siv = RealValuedOperands.similarityInVariance(refValue,oValue,c2);
                    }
                    outIter.setSample(c, r, 0, siv);
                }
            }
        } finally {
            outIter.done();
            refIter.done();
            oIter.done();
        }

        return CoverageUtilities.buildCoverage("siv", outWR, regionMap, referenceMap.getCoordinateReferenceSystem());

    }

    private static GridCoverage2D calculateSimPattern(GridCoverage2D covarianceMap, GridCoverage2D referenceMap, GridCoverage2D otherMap, double c3){

        RandomIter refIter = CoverageUtilities.getRandomIterator(referenceMap);
        RandomIter oIter = CoverageUtilities.getRandomIterator(otherMap);
        RandomIter cIter = CoverageUtilities.getRandomIterator(covarianceMap);

        double refNV = HMConstants.getNovalue(referenceMap);
        double oNV = HMConstants.getNovalue(otherMap);
        double cNV = HMConstants.getNovalue(covarianceMap);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(referenceMap);
        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster outWR = CoverageUtilities.createWritableRaster(cols, rows, null, null, refNV);
        WritableRandomIter outIter = CoverageUtilities.getWritableRandomIterator(outWR);

        double sip;

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {
                    double refValue = refIter.getSampleDouble(c, r, 0);
                    double oValue = oIter.getSampleDouble(c,r,0);
                    double covValue = cIter.getSampleDouble(c,r,0);

                    sip = refNV;
                    if (!HMConstants.isNovalue(refValue, refNV) && !HMConstants.isNovalue(oValue, oNV) && !HMConstants.isNovalue(covValue, cNV)) {
                        sip = RealValuedOperands.similarityInPattern(covValue,refValue,oValue,c3);
                    }
                    outIter.setSample(c, r, 0, sip);
                }
            }
        } finally {
            outIter.done();
            refIter.done();
            oIter.done();
        }

        return CoverageUtilities.buildCoverage("sip", outWR, regionMap, referenceMap.getCoordinateReferenceSystem());

    }

    private GridCoverage2D calculateSimStructural(){

        RandomIter mIter = CoverageUtilities.getRandomIterator(this.simMean);
        RandomIter vIter = CoverageUtilities.getRandomIterator(this.simVariance);
        RandomIter pIter = CoverageUtilities.getRandomIterator(this.simPattern);

        double mNV = HMConstants.getNovalue(this.simMean);
        double vNV = HMConstants.getNovalue(this.simVariance);
        double pNV = HMConstants.getNovalue(this.simPattern);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(this.simMean);
        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster outWR = CoverageUtilities.createWritableRaster(cols, rows, null, null, mNV);
        WritableRandomIter outIter = CoverageUtilities.getWritableRandomIterator(outWR);

        double ssim;

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {
                    double mValue = mIter.getSampleDouble(c,r,0);
                    double vValue = vIter.getSampleDouble(c,r,0);
                    double pValue = pIter.getSampleDouble(c,r,0);

                    ssim = mNV;
                    if (!HMConstants.isNovalue(mValue, mNV) && !HMConstants.isNovalue(vValue, vNV) && !HMConstants.isNovalue(pValue, pNV)) {
                        ssim = RealValuedOperands.structuralSimilarity(mValue,vValue,pValue,1.0,1.0,1.0);
                    }
                    outIter.setSample(c, r, 0, ssim);
                }
            }
        } finally {
            outIter.done();
            mIter.done();
            vIter.done();
            pIter.done();
        }

        return CoverageUtilities.buildCoverage("sip", outWR, regionMap, this.simMean.getCoordinateReferenceSystem());

    }


    public enum SimilarityType {
        MEAN,
        VARIANCE,
        PATTERN,
        STRUCTURAL;
    }



}
