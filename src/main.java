import GeneticAlgorithm.Framework;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileNotFoundException;

import java.io.IOException;

public class main {

    public static void main(String[] agrs) throws FileNotFoundException, CsvValidationException{

        try {
            Framework GA = new Framework(".\\src\\Config\\config.properties");
            GA.startAll();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
