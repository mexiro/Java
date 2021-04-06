import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Merge_all_inDir {

    public static void main(String[] args) throws IOException {


            File folder = new File("v:\\Scan Files");
            File[] listOfFiles = folder.listFiles();

     /* TODO FIRST Rename to text */
            for (int i = 0; i < listOfFiles.length; i++) {

                if (listOfFiles[i].isFile()) {

                    //  get the name of that file and check if it is text //
                    String emri = String.valueOf(listOfFiles[i]);
                    String substringu = emri.substring(emri.length() - 3);


                    if  (!substringu.equals("txt")) {
                        File f = new File("v:\\Scan Files\\" + listOfFiles[i].getName());
                        f.renameTo(new File("v:\\Scan Files\\" + listOfFiles[i].getName() + ".txt"));
                       // System.out.println("y be");
                    }
                }
            }
         System.out.println("Files renamed to text ");



    /* TODO merge in one  */
            String outputdir = "v:\\merged\\Merged.txt";
            Path output = Paths.get(outputdir);
            Path directory = Paths.get("v:\\Scan Files");
            Stream<Path> filesToProcess = Files.list(directory);

            // first delete previous merged and create a new on//
            File file = new File("v:\\merged\\Merged.txt");
            try {
                boolean result = Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                System.out.println("error deleteprev file:"+e);
                e.printStackTrace();
            }


        PrintWriter writer = new PrintWriter(outputdir, "UTF-8");
            // Iterate all files
            filesToProcess.forEach(path -> {
                // Get all lines of that file
                Stream<String> lines = null;
                try {
                    lines = Files.lines(path);

                } catch (IOException e) {
                    System.out.println("error lines path:"+e);
                    e.printStackTrace();
                }


                // Iterate all lines
                lines.forEach(line -> {
                    // Append the line separator
                    String lineToWrite = line; // + System.lineSeparator(); Nese duhet  //


                    // Write every line to the output file

                        //Files.write(output, lineToWrite.getBytes(StandardCharsets.UTF_8));
                        //Files.write(output, Collections.singleton(lineToWrite),StandardCharsets.UTF_8);
                        writer.println(lineToWrite);
                        // Debug System.out.println(lineToWrite);
                });
            });
        writer.close();  System.out.println("Multiple files merged to one sucesfully");
    }
}


