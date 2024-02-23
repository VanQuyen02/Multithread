package MyThread;

import Data.GeneticData;
import Entity.Individual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GenerateThread implements Runnable {

    private GeneticData geneticData;

    private Individual parent1;
    private Individual parent2;
    private BlockingBuffer newPopulation;
    private ArrayList<Individual> children;
    private double mutationRate;
    private double crossoverRate;

    public GenerateThread(BlockingBuffer newPopulation, GeneticData geneticData, ArrayList<Individual> population, double crossoverRate, double mutationRate) {
        this.geneticData = geneticData;
        this.newPopulation = newPopulation;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;

        Collections.shuffle(population);
        parent1 = population.get(0);
        parent2 = population.get(1);

        children = new ArrayList<>();
        children.add(parent1.clone());
        children.add(parent2.clone());
    }

    private void crossover() {
        Random rand = new Random();
        int[][][] chromosome1 = parent1.getChromosome().clone();
        int[][][] chromosome2 = parent2.getChromosome().clone();
        for (int s = 0; s < chromosome1.length; s++) {
            if (rand.nextDouble() < crossoverRate) {
                int[][] temp = chromosome1[s];
                chromosome1[s] = chromosome2[s];
                chromosome2[s] = temp;
                if (parent1.passAllConstraints(chromosome1) && parent1.passAllConstraints(chromosome2)) {
                    continue;
                } else {
                    int[][] temp1 = chromosome1[s];
                    chromosome1[s] = chromosome2[s];
                    chromosome2[s] = temp1;
                }
            }
        }
    }

    private void mutation() {
        Random rand = new Random();
        int[][][] chromosome1 = parent1.getChromosome().clone();
        int subjectIndex1 = rand.nextInt(chromosome1.length);
        int subjectIndex2 = rand.nextInt(chromosome1.length);
        while (subjectIndex1 == subjectIndex2) {
            subjectIndex2 = rand.nextInt(chromosome1.length);
        }
        int[][] subjectHeldAtSlot = parent1.createSubjectHeldAtSlot(chromosome1);
        int[][] slotStartOfSubject = parent1.createSlotStartOfSubject(subjectHeldAtSlot);
        int[] slotStart1 = slotStartOfSubject[subjectIndex1];
        int[] slotStart2 = slotStartOfSubject[subjectIndex2];
        int lengthS1 = parent1.data.lengthOfSubject[subjectIndex1];
        int lengthS2 = parent1.data.lengthOfSubject[subjectIndex2];
        if (slotStart1 == slotStart2) {
            return;
        }
        if (slotStart1[0] + lengthS2 >= chromosome1[0].length || slotStart2[0] + lengthS1 >= chromosome1[0].length) {
            return;
        }
        List<Integer> inviSubject1 = new ArrayList<>();
        List<Integer> inviSubject2 = new ArrayList<>();
        for (int i = 0; i < chromosome1[0][0].length; i++) {
            for (int t = slotStart1[0]; t < slotStart1[0] + lengthS1; t++) {
                if (chromosome1[subjectIndex1][t][i] == 1) {
                    inviSubject1.add(i);
                    chromosome1[subjectIndex1][t][i] = 0;
                }
            }
            for (int t = slotStart2[0]; t < slotStart2[0] + lengthS2; t++) {
                if (chromosome1[subjectIndex2][t][i] == 1) {
                    inviSubject2.add(i);
                    chromosome1[subjectIndex2][t][i] = 0;
                }
            }
        }
        for (int i = 0; i < chromosome1[0][0].length; i++) {
            for (int t = slotStart1[0]; t < slotStart1[0] + lengthS2; t++) {
                if (inviSubject2.contains(i)) {
                    chromosome1[subjectIndex2][t][i] = 1;
                }
            }
            for (int t = slotStart2[0]; t < slotStart2[0] + lengthS1; t++) {
                if (inviSubject1.contains(i)) {
                    chromosome1[subjectIndex1][t][i] = 1;
                }
            }
        }
        slotStartOfSubject[subjectIndex1] = slotStart2;
        slotStartOfSubject[subjectIndex2] = slotStart1;
        if (!parent1.passAllConstraints(chromosome1)) {
            slotStart1 = slotStartOfSubject[subjectIndex1];
            slotStart2 = slotStartOfSubject[subjectIndex2];
            inviSubject1 = new ArrayList<>();
            inviSubject2 = new ArrayList<>();
            for (int i = 0; i < chromosome1[0][0].length; i++) {
                for (int t = slotStart1[0]; t < slotStart1[0] + lengthS1; t++) {
                    if (chromosome1[subjectIndex1][t][i] == 1) {
                        inviSubject1.add(i);
                        chromosome1[subjectIndex1][t][i] = 0;
                    }
                }
                for (int t = slotStart2[0]; t <  slotStart2[0] + lengthS2; t++) {
                    if (chromosome1[subjectIndex2][t][i] == 1) {
                        inviSubject2.add(i);
                        chromosome1[subjectIndex2][t][i] = 0;
                    }
                }
            }
            for (int i = 0; i < chromosome1[0][0].length; i++) {
                for (int t = slotStart1[0]; t < slotStart1[0] + lengthS2; t++) {
                    if (inviSubject2.contains(i)) {
                        chromosome1[subjectIndex2][t][i] = 1;
                    }
                }
                for (int t = slotStart2[0]; t < slotStart2[0] + lengthS1; t++) {
                    if (inviSubject1.contains(i)) {
                        chromosome1[subjectIndex1][t][i] = 1;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        //crossover
        if (Math.random() < crossoverRate) {
            crossover();
        }

        //mutation
        if (Math.random() < mutationRate) {
            mutation();
        }

        children.stream().forEach(i -> {
            try {
                newPopulation.push(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
