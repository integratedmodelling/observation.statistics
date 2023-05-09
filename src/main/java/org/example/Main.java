package org.example;


import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.map.comparison.MapComparator;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("Attempting pixel-wise comparison of Gabon carbon stocks between 2001 and 2002:");

        // Currently for testing. Replace reference and other paths by a suitable local version.
        // TODO: Write a proper testcase.
        MapComparator mc = new MapComparator("/home/dibepa/git/observation.statistics/data/test/klab_gabon_2001.tiff","/home/dibepa/git/observation.statistics/data/test/klab_gabon_2002.tiff");
        GridCoverage2D diffMap = mc.pixelWiseDifference(false,false);

        String url = "/home/dibepa/git/observation.statistics/data/test/klab_gabon_2001_2002_difference.tif";
        File file = new File(url);
        GeoTiffWriter writer = new GeoTiffWriter(file);
        writer.write(diffMap, null);
        writer.dispose();



    }
}