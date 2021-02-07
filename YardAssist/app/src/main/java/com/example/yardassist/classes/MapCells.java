package com.example.yardassist.classes;

public class MapCells {



    public static double getDistanceM(double lat1, double lon1, double lat2, double lon2){
        double r = 6378.137;
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = r * c;
        return d * 1000;
    }

    public double getDistanceC(double lat1, double lon1, double lat2, double lon2){
        double d = 0;
        if(lat1 == lat2){
            d = lon1 - lon2;
        }
        else if(lon1 == lon2){
            d = lat1 - lat2;
        }
        return d;
    }

    private static Coordinate rotator(Coordinate a, Coordinate center){
        Coordinate c = new Coordinate();
        Coordinate t = new Coordinate(center.lat - a.lat, center.lon - a.lon );
        Coordinate r = new Coordinate(-t.lon, t.lat);
        c.lat = r.lat + a.lat;
        c.lon = r.lon + a.lon;
        return c;
    }
    public static Coordinate[] findUnknownCorners(Coordinate a, Coordinate b){ //rotates point center around point a -90 degrees
        Coordinate[] corners = new Coordinate[2];
        Coordinate center = new Coordinate();
        center.lat = (a.lat + b.lat)/2  ; center.lon = (a.lon + b.lon)/2  ; //centerpoint
        corners[0] = rotator(a, center);
        corners[1] = rotator(b, center);

        return corners;
    }

    private Coordinate getCDiff(Coordinate a, Coordinate b, double n) {
        Coordinate t = new Coordinate();
        t.lat = (b.lat - a.lat) / n;
        t.lon = (b.lon - a.lon) / n;
        return t;
    }

  /*  public docOfCells mapCells2(Coordinate coord1, Coordinate coord2){
        Coordinate[] uCorners = findUnknownCorners(coord1, coord2);
        Coordinate c = uCorners[0];
        Coordinate d = uCorners[1];



        double h = getDistanceM(coord1.lat, coord1.lon, d.lat, d.lon);
        double b = getDistanceM(coord2.lat, coord2.lon, d.lat, d.lon);
        int n1 = (int) Math.ceil(h/3);
        int n2 = (int) Math.ceil(b/3);

        docOfCells mapGrid = new docOfCells();
        Coordinate cDiffB = getCDiff(coord1, c, (double) n2);
        Coordinate cDiffH = getCDiff(coord1, d, (double) n1);


        for(int i = 0; i < n1; i++){
            rowOfCells row = new rowOfCells();
            for(int j = 0; j < n2; j++){
                Coordinate top = new Coordinate(coord1.lat + ((j * cDiffB.lat) + (i * cDiffH.lat) ), coord1.lon + (j * cDiffB.lon) + (i * cDiffH.lon));

                Coordinate bottom = new Coordinate(coord1.lat + (((j+1) * cDiffB.lat) + ((i+1) * cDiffH.lat) ), coord1.lon + ((j+1) * cDiffB.lon) + ((i+1) * cDiffH.lon));


                row.cells.add(new Cell(top, bottom, i, j));

            }
            row.row = i;
            mapGrid.cells.add(row);
        }
        mapGrid.rows = n1;
        mapGrid.columns = n2;
        return mapGrid;
    } */

    public docOfCells mapCells2(Coordinate topLeft, Coordinate topRight, Coordinate botLeft){
        double cellSize = 3;
        double topLine = getDistanceM(topLeft.lat, topLeft.lon, topRight.lat, topRight.lon); //the distance between the 2 top points (width)
        double hLine = getDistanceM(topLeft.lat, topLeft.lon, botLeft.lat, botLeft.lon); //the distance between the top and bottom (height)

        int nColumns = (int) Math.ceil(topLine/cellSize); // get how many cells for the width
        int nRows = (int) Math.ceil(hLine/cellSize); // get how many cells for the height

        docOfCells mapGrid = new docOfCells();
        Coordinate cDiffB = getCDiff(topLeft, topRight, (double) nColumns);
        Coordinate cDiffH = getCDiff(topLeft, botLeft, (double) nRows);


        for(int i = 0; i < nRows; i++){
            rowOfCells row = new rowOfCells();
            for(int j = 0; j < nColumns; j++){
                Coordinate topL = new Coordinate(topLeft.lat + ((j * cDiffB.lat) + (i * cDiffH.lat) ), topLeft.lon + (j * cDiffB.lon) + (i * cDiffH.lon));
                Coordinate topR = new Coordinate(topLeft.lat + (((j+1) * cDiffB.lat) + (i * cDiffH.lat) ), topLeft.lon + ((j+1) * cDiffB.lon) + (i * cDiffH.lon));
                Coordinate botL = new Coordinate(topLeft.lat + ((j * cDiffB.lat) + ((i+1) * cDiffH.lat) ), topLeft.lon + (j * cDiffB.lon) + ((i+1) * cDiffH.lon));
                Coordinate botR = new Coordinate(topLeft.lat + (((j+1) * cDiffB.lat) + ((i+1) * cDiffH.lat) ), topLeft.lon + ((j+1) * cDiffB.lon) + ((i+1) * cDiffH.lon));


                row.cells.add(new Cell(topL, topR, botL, botR, i, j));

            }
            row.row = i;
            mapGrid.cells.add(row);
        }
        mapGrid.rows = nRows;
        mapGrid.columns = nColumns;
        return mapGrid;
    }
}
