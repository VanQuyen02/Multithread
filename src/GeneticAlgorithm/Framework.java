package GeneticAlgorithm;

import Data.GeneticData;
import Entity.Individual;
import MyThread.BlockingBuffer;
import MyThread.GenerateThread;
import MyThread.InitializationThread;
import ObjectiveFunction.FitnessCalculator;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private FitnessCalculator fitnessCalculator;

    public Framework(String configPath) throws IOException, FileNotFoundException, CsvValidationException {

        //read Config
        readConfig(configPath);

        //read input
        geneticData = new GeneticData();
        geneticData.loadData();
        //generate fitnessCalculator
        fitnessCalculator = new FitnessCalculator(geneticData);

        //initialize population
        population = new ArrayList<>();

    }

    public void startAll() throws InterruptedException, IOException {
        //Initialization
        initialization();

        //Calculate fitness
        calcuateFitness();

        float bestFitness = Float.POSITIVE_INFINITY;
        int currentConvect = 0;
        Individual bestSolution = null;

        BlockingBuffer newPopulation = new BlockingBuffer(numPopulation);
        //For each generation
        for (int i = 0; i < loops; i++) {
            //Selection
            int numElite = selection(newPopulation);
            int numGenerate = numPopulation - numElite;
            //Generate
            generate(newPopulation, numGenerate);

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
            for (int k = 0; k < 10; k++) {
                System.out.println(population.get(k).getFitness());
            }
            System.out.println();
        }
        String filePath = "data/output.txt";
        bestSolution.writeChromosomeToFile(filePath, bestSolution.chromosome);
    }

    public void calcuateFitness() {
        population.stream().forEach(i -> i.setFitness(fitnessCalculator.calculateFitess(i.getChromosome())));
    }

    private int selection(BlockingBuffer newPopulation) throws InterruptedException {

        int eliteNumber = numPopulation / elitePer;

        for (int i = 0; i < eliteNumber; i++) {
            newPopulation.push(population.get(i));
        }

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

    public void generate(BlockingBuffer newPopulation, int numNewIndividual) {
        //initialize executor
        ExecutorService executorService = Executors.newFixedThreadPool(generateThreadNum);

        //initialize population
        for (int i = 0; i < numNewIndividual; i++) {
            //generate thread
            Runnable thread = new GenerateThread(newPopulation, geneticData, population, crossoverRate, mutationRate);

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

    public void log() {

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
