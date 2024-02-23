package MyThread;

import Data.GeneticData;
import Entity.Individual;

public class InitializationThread implements Runnable{

    private BlockingBuffer population;
    private GeneticData geneticData;

    public InitializationThread() {

    }

    public InitializationThread(BlockingBuffer population, GeneticData geneticData){
        this.population = population;
        this.geneticData = geneticData;
    }

    @Override
    public void run() {
        //create new Individual
        Individual newIndividual = generateIndividual();

        //add to buffer queues
        try {
            population.push(newIndividual);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    

    private Individual generateIndividual(){
        Individual newIndividual = new Individual();
        newIndividual.setData(geneticData);
        newIndividual.setChromosome(newIndividual.createChromosome());
//        System.out.println("Create new Individual");

        return newIndividual;
    }
}