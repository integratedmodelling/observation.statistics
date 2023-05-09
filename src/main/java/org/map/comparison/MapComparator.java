package org.map.comparison;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.hortonmachine.gears.io.rasterreader.OmsRasterReader;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.RegionMap;
import org.hortonmachine.gears.utils.coverage.CoverageUtilities;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.WritableRandomIter;
import java.awt.image.WritableRaster;

import static org.operations.RealValuedOperands.difference;

public class MapComparator {

    private final GridCoverage2D referenceMap;
    private final GridCoverage2D otherMap;

    public MapComparator( String referencePath, String otherPath ) throws Exception {

        OmsRasterReader referenceReader = new OmsRasterReader();
        referenceReader.file = referencePath;

        OmsRasterReader otherReader = new OmsRasterReader();
        otherReader.file = otherPath;

        referenceReader.process();
        otherReader.process();

        this.referenceMap = referenceReader.outRaster;
        this.otherMap = otherReader.outRaster;
    }

    public GridCoverage2D pixelWiseDifference( boolean relative, boolean signed ){

        RandomIter refIter = CoverageUtilities.getRandomIterator(this.referenceMap);
        RandomIter oIter = CoverageUtilities.getRandomIterator(this.otherMap);

        double refNV = HMConstants.getNovalue(this.referenceMap);
        double oNV = HMConstants.getNovalue(this.otherMap);

        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(this.referenceMap);
        int rows = regionMap.getRows();
        int cols = regionMap.getCols();

        WritableRaster comparisonResult = CoverageUtilities.createWritableRaster(cols, rows, null, null, refNV);
        WritableRandomIter resIter = CoverageUtilities.getWritableRandomIterator(comparisonResult);

        double diff;

        try {
            for(int r = 0; r < rows; r++) {
                for(int c = 0; c < cols; c++) {
                    double refValue = refIter.getSampleDouble(c, r, 0);
                    double oValue = oIter.getSampleDouble(c,r,0);
                    diff = refNV;
                    if (!HMConstants.isNovalue(refValue, refNV) && !HMConstants.isNovalue(oValue, oNV)) {
                        diff = difference(refValue,oValue,relative,signed);
                    }
                    resIter.setSample(c, r, 0, diff);
                }
            }
        } finally {
            resIter.done();
            refIter.done();
            oIter.done();
        }
        GridCoverageFactory factory = new GridCoverageFactory();
        return factory.create("comparison", comparisonResult, referenceMap.getEnvelope2D());

    }

    public GridCoverage2D structuralSimilarityIndex(int radius) {

        StructuralSimilarity ssim = new StructuralSimilarity(this.referenceMap,this.otherMap,radius);
        return ssim.getSimStructuralSimilarity();

    }
}
