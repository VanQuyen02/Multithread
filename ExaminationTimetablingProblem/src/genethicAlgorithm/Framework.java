package genethicAlgorithm;

import com.opencsv.exceptions.CsvValidationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import myThread.BlockingBuffer;
import myThread.FitnessCalculateThread;
import myThread.GenerateThread;
import myThread.InitializationThread;
import dataInput.GeneticData;
import entities.Individual;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Framework {

    private int numPopulation;
    private int loops;
    private int terminal;
    private int initializationThreadNum;
    private int elitePer;
    private double crossoverRate;
    private double mutationRate;
    private int generateThreadNum;
    private ArrayList<Individual> population;
    private GeneticData geneticData;
    Random rand = new Random();

    public Framework(String configPath) throws IOException, FileNotFoundException, CsvValidationException {

        //read Config
        readConfig(configPath);

        //read input
        geneticData = new GeneticData();
        geneticData.loadData();
        //generate fitnessCalculator

        //initialize population
        population = new ArrayList<>();

    }

    public void startAll() throws InterruptedException, IOException {
        //Initialization
        initialization();

        //Calculate fitness
        calcuateFitness();
        double bestFitness = Double.POSITIVE_INFINITY;
        ArrayList<Double> bestResult = new ArrayList<>();
        int currentConvect = 0;
        Individual bestSolution = null;

        BlockingBuffer newPopulation = new BlockingBuffer(numPopulation);
        //For each generation
        for (int i = 0; i < loops; i++) {

            if (i % 100 == 0 && !readFunc()) {
                break;
            }
            //Selection

            int numElite = selection(newPopulation);
            int numGenerate = (numPopulation - numElite);
            //Generate
            generate(newPopulation, numGenerate, i);

            //Calculate fitness
            calcuateFitness();

            //Log
            Collections.sort(population);
            if (bestFitness > population.get(0).getFitness()) {
                bestFitness = population.get(0).getFitness();
                bestSolution = population.get(0);
                currentConvect = 0;
            } else {
                currentConvect++;
                if (currentConvect > terminal) {
                    break;
                }
            }
            System.out.println(String.format("Generation %d best fitness: %f", i, bestFitness));
            bestResult.add(population.get(0).getFitness());
            for (int k = 0; k < 10; k++) {
                System.out.println(population.get(k).getFitness());
            }
            System.out.println();
        }
        String resultFilePath = "data/fitness.txt";
        writeResultToFile(resultFilePath, bestResult);
        String chromosomefilePath = "data/output.txt";
        bestSolution.writeChromosomeToFile(chromosomefilePath, bestSolution.getChromosome());
    }

    private void writeResultToFile(String filePath, ArrayList<Double> bestResult) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        for (int i = 0; i < bestResult.size(); i++) {
            String line = String.format("%f\n", bestResult.get(i));
            writer.write(line);
        }
        writer.close();
    }

    public boolean readFunc() throws IOException {
        String filePath = ".\\src\\configure\\config.properties";
        InputStream inputStream = new FileInputStream(filePath);

        //load config
        Properties prop = new Properties();

        prop.load(inputStream);

        int x = Integer.parseInt(prop.getProperty("system.isRun"));
        return x == 1;
    }

    public void calcuateFitness() {
        ExecutorService executorService = Executors.newFixedThreadPool(initializationThreadNum);

        //initialize population
        for (int i = 0; i < numPopulation; i++) {
            //generate thread
            Runnable thread = new FitnessCalculateThread(population.get(i));

            executorService.execute(thread);
        }

        //shutdown thread
        executorService.shutdown();

        while (!executorService.isTerminated()) {

        }

        System.out.println("Finish calculation phase");
    }

    private int selection(BlockingBuffer newPopulation) throws InterruptedException {

        int eliteNumber = numPopulation / elitePer;
        Collections.sort(population);
        for (int i = 0; i < eliteNumber; i++) {
            newPopulation.push(population.get(i));
        }
//        int cnt = 0;
//        for (int i = 0; i < numPopulation; i++) {
//            if (i > 1 && population.get(i).getFitness() == population.get(i - 1).getFitness()) {
//                continue;
//            }
//            cnt++;
//            newPopulation.push(population.get(i));
//            if (cnt >= eliteNumber) {
//                break;
//            }
//        }
//        while (cnt < eliteNumber) {
//            int id = rand.nextInt(eliteNumber);
//            newPopulation.push(population.get(id));
//            cnt++;
//        }
        return eliteNumber;

    }

    public int initialization() {
        BlockingBuffer initializationPopulation = new BlockingBuffer(numPopulation);
        //initialize executor
        ExecutorService executorService = Executors.newFixedThreadPool(initializationThreadNum);

        //initialize population
        for (int i = 0; i < numPopulation; i++) {
            //generate thread
            Runnable thread = new InitializationThread(initializationPopulation, geneticData);

            executorService.execute(thread);
        }

        //shutdown thread
        executorService.shutdown();

        while (!executorService.isTerminated()) {

        }

        System.out.println("Finish initiation phase");

        return initializationPopulation.drainTo(population);
    }

    public void generate(BlockingBuffer newPopulation, int numNewIndividual, int generation) {
        //initialize executor
        ExecutorService executorService = Executors.newFixedThreadPool(generateThreadNum);

        //initialize population
        for (int i = 0; i < numNewIndividual; i++) {
            //generate thread
            Runnable thread = new GenerateThread(newPopulation, geneticData, population, crossoverRate, mutationRate, generation);

            executorService.execute(thread);
        }

        //shutdown thread
        executorService.shutdown();
        while (!executorService.isTerminated()) {

        }
        System.out.println("Finish generate phase");

        population.clear();
        newPopulation.drainTo(population);
    }

    private void readConfig(String configPath) throws IOException {

        //load stream
        InputStream inputStream = new FileInputStream(configPath);

        //load config
        Properties prop = new Properties();

        prop.load(inputStream);

        loops = Integer.parseInt(prop.getProperty("genetic.loops"));
        numPopulation = Integer.parseInt(prop.getProperty("genetic.numPopulation"));
        terminal = Integer.parseInt(prop.getProperty("genetic.terminal"));
        elitePer = Integer.parseInt(prop.getProperty("genetic.elitePercent"));
        crossoverRate = Double.parseDouble(prop.getProperty("genetic.crossoverRate"));
        mutationRate = Double.parseDouble(prop.getProperty("genetic.mutationRate"));
        generateThreadNum = Integer.parseInt(prop.getProperty("genetic.generateThreadNum"));
        initializationThreadNum = Integer.parseInt(prop.getProperty("genetic.initializationThreadNum"));
//        weights = Arrays.stream(prop.getProperty("objectives.weights").split(",")).mapToDouble(Double::parseDouble).toArray();

    }

}
