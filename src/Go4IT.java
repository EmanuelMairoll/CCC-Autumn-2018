import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Go4IT implements FileParsing.ContentHandler {


    public static void main(String[] args) {
        FileParsing.parseFolder("levels/level4", new Go4IT());
    }

    @Override
    public void handle(String[] params, String filename) {
        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(params));
        String[] firstLine = lines.remove(0).split(" ");

        long startTimestamp = Long.parseLong(firstLine[0]);
        long endTimestamp = Long.parseLong(firstLine[1]);
        Image[] images = new Image[Integer.parseInt(firstLine[2])];

        for (int imageIndex = 0; imageIndex < images.length; imageIndex++) {
            String[] imageHeader = lines.remove(0).split(" ");
            long timestamp = Long.parseLong(imageHeader[0]);
            int rowCount = Integer.parseInt(imageHeader[1]);
            int colCount = Integer.parseInt(imageHeader[2]);
            List<String> rows = new ArrayList<>();

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                rows.add(lines.remove(0));
            }

            images[imageIndex] = new Image(rowCount, colCount, rows, timestamp);
        }

        AsteroidManager manager = new AsteroidManager();
        for (Image image : images) {
            manager.addImage(image);
        }

        manager.cleanup(startTimestamp, endTimestamp);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output/" + filename));

            for (Asteroid asteroid : manager.listAsteroids()) {
                String output = "";
                output += asteroid.occurrences.get(0).timestamp + " ";
                output += asteroid.occurrences.get(asteroid.occurrences.size() - 1).timestamp + " ";
                output += asteroid.occurrences.size() + "";
                System.out.println(output);
                writer.write(output);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static class Image {
        long timestamp;
        int data[][];

        public Image(int rowCount, int colCount, List<String> rows, long timestamp) {
            data = new int[rowCount][colCount];
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                String[] cols = rows.get(rowIndex).split(" ");

                for (int colIndex = 0; colIndex < cols.length; colIndex++) {
                    data[rowIndex][colIndex] = Integer.parseInt(cols[colIndex]);
                }
            }

            this.timestamp = timestamp;
        }

        private Image() {

        }

        private static Image rotate(Image image, int i) {
            Image newImage = image.copy();

            if (i == 2) {
                for (int row = 0; row < image.getRowCount(); row++) {
                    for (int col = 0; col < image.getColCount(); col++) {
                        newImage.data[row][col] = image.data[image.getRowCount() - 1 - row][image.getColCount() - 1 - col];
                    }
                }
            }

            if (i % 2 == 1) {
                newImage.data = new int[image.getColCount()][image.getRowCount()];
                for (int row = 0; row < image.getRowCount(); row++) {
                    for (int col = 0; col < image.getColCount(); col++) {
                        newImage.data[col][row] = image.data[row][image.getColCount() - col - 1];
                    }
                }
            }
            return newImage;
        }

        public static Image getOccurrenceAtTimestamp(List<Image> occurrences, long timestamp) {
            for (Image occurrence : occurrences) {
                if (occurrence.timestamp == timestamp) {
                    return occurrence;
                }
            }
            return null;
        }

        private Image copy() {
            Image newImage = new Image();
            newImage.timestamp = timestamp;
            newImage.data = data.clone();
            return newImage;
        }

        public boolean isOccurrence() {
            for (int row = 0; row < data.length; row++) {
                for (int col = 0; col < data[row].length; col++) {
                    if (data[row][col] != 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        public int getRowCount() {
            return data.length;
        }

        public int getColCount() {
            return data[0].length;
        }

        public boolean hasSameShape(Image otherImageUntransformed) {
            for (int i = 0; i < 1; i++) {
                Image otherImage = Image.rotate(otherImageUntransformed, i);


                OIRowOffset:
                for (int rowOffset = 1 - otherImage.getRowCount(); rowOffset < this.getRowCount(); rowOffset++) {
                    OIColOffset:
                    for (int colOffset = 1 - otherImage.getColCount(); colOffset < this.getColCount(); colOffset++) {

                        IterateThisImageRows:
                        for (int thisRow = 0; thisRow < this.getRowCount(); thisRow++) {
                            IterateThisImageCols:
                            for (int thisCol = 0; thisCol < this.getColCount(); thisCol++) {
                                if (!(thisRow >= rowOffset && thisCol >= colOffset && thisRow < rowOffset + otherImage.getRowCount() && thisCol < colOffset +
                                        otherImage.getColCount())) {
                                    if (data[thisRow][thisCol] != 0) {
                                        continue OIColOffset;
                                    }
                                }
                            }
                        }

                        IterateOtherImageRows:
                        for (int otherRow = 0; otherRow < otherImage.getRowCount(); otherRow++) {
                            IterateOtherImageCols:
                            for (int otherCol = 0; otherCol < otherImage.getColCount(); otherCol++) {
                                if (!(otherRow >= -rowOffset && otherCol >= -colOffset && otherRow < -rowOffset + this.getRowCount() && otherCol < -colOffset
                                        + this.getColCount())) {
                                    if (otherImage.data[otherRow][otherCol] != 0) {
                                        continue OIColOffset;
                                    }
                                }
                            }
                        }

                        IterateIntersectionRows:
                        for (int row = Math.max(0, rowOffset); row < Math.min(this.getRowCount(), otherImage.getRowCount() + rowOffset); row++) {
                            IterateIntersectionCols:
                            for (int col = Math.max(0, colOffset); col < Math.min(this.getColCount(), otherImage.getColCount() + colOffset); col++) {
                                boolean thisIsNull = data[row][col] == 0;
                                boolean otherIsNull = otherImage.data[row - rowOffset][col - colOffset] == 0;

                                if (thisIsNull != otherIsNull) {
                                    continue OIColOffset;
                                }
                            }
                        }

                        return true;
                    }
                }
            }
            return false;
        }

        public int roationRelativeTo(Image otherImage) {
            for (int i = 0; i > 4; i++) {
                if (this.hasSameShape(Image.rotate(otherImage, i))) {
                    return i;
                }
            }
            return -1;

        }

    }

    public static class AsteroidManager {
        List<Asteroid> asteroids = new ArrayList<>();

        public Asteroid addImage(Image image) {
            if (!image.isOccurrence()) {
                return null;
            }

            for (Asteroid asteroid : asteroids) {
                if (asteroid.getFirstOccurrence().hasSameShape(image)) {
                    asteroid.addOccurrence(image);
                    return asteroid;
                }
            }

            Asteroid newAsteroid = new Asteroid(image);
            asteroids.add(newAsteroid);
            return newAsteroid;
        }

        public List<Asteroid> listAsteroids() {
            return asteroids;
        }

        public void cleanup(long startTimestamp, long endTimestamp) {
            List<Asteroid> cleanedList = new ArrayList<>();
            for (Asteroid asteroid : asteroids) {
                cleanedList.addAll(asteroid.splitByFrequency(startTimestamp, endTimestamp));
            }

            cleanedList.sort(Comparator.comparingLong(a -> a.getFirstOccurrence().timestamp));
            asteroids = cleanedList;
        }
    }

    public static class Asteroid {
        List<Image> occurrences = new ArrayList<>();

        public Asteroid(Image firstOccurrence) {
            occurrences.add(firstOccurrence);
        }

        public Asteroid(List<Image> occurrences) {
            this.occurrences.addAll(occurrences);
        }

        public Image getFirstOccurrence() {
            return occurrences.get(0);
        }

        public void addOccurrence(Image image) {
            occurrences.add(image);
        }

        public List<Asteroid> splitByFrequency(long startTimestamp, long endTimestamp) {
            List<Image> occurrences = new ArrayList<>(this.occurrences);
            List<Asteroid> splittedAsteroids = new ArrayList<>();
            while (occurrences.size() > 0) {
                Image rowStarter = occurrences.get(0);

                boolean firstItem = true;
                findNextInRow:
                for (Image nextInRow : occurrences) {
                    if (firstItem) {
                        firstItem = false;
                        continue;
                    }


                    long T = nextInRow.timestamp - rowStarter.timestamp;
                    long initial = rowStarter.timestamp;
                    long rot = rowStarter.roationRelativeTo(nextInRow);

                    List<Image> possibleRow = new ArrayList<>();
                    possibleRow.add(rowStarter);

                    int i = 0;
                    for (long timeToLookFor = initial + T; timeToLookFor <= endTimestamp; timeToLookFor += T) {
                        i++;
                        Image occurrence;
                        if ((occurrence = Image.getOccurrenceAtTimestamp(occurrences, timeToLookFor)) == null || rowStarter.roationRelativeTo(occurrence) !=
                                (rot * i) % 4) {
                            continue findNextInRow;
                        } else {
                            possibleRow.add(occurrence);
                        }
                    }

                    i = 0;
                    for (long timeToLookFor = initial - T; timeToLookFor >= startTimestamp; timeToLookFor -= T) {
                        i--;
                        Image occurrence;
                        if ((occurrence = Image.getOccurrenceAtTimestamp(occurrences, timeToLookFor)) == null || rowStarter.roationRelativeTo(occurrence) !=
                                (rot * i) % 4) {
                            continue findNextInRow;
                        } else {
                            possibleRow.add(occurrence);
                        }
                    }

                    if (possibleRow.size() >= 4) {
                        occurrences.removeAll(possibleRow);
                        splittedAsteroids.add(new Asteroid(possibleRow));
                        break;
                    }
                }

            }

            return splittedAsteroids;
        }
    }

}
