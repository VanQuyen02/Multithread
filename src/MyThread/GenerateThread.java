package myThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import utils.GeneticData;
import utils.Individual;

public class GenerateThread implements Runnable {

    private GeneticData data;

    private Individual parent1;
    private Individual parent2;
    private Individual children1;
    private Individual children2;
    private BlockingBuffer newPopulation;
    private ArrayList<Individual> children;
    private double mutationRate;
    private double crossoverRate;
    Random rand = new Random();

    public GenerateThread(BlockingBuffer newPopulation, GeneticData geneticData, ArrayList<Individual> population, double crossoverRate, double mutationRate) {
        this.data = geneticData;
        this.newPopulation = newPopulation;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;

        Collections.shuffle(population);
        parent1 = population.get(0);
        parent2 = population.get(1);

        children = new ArrayList<>();
        children1 = parent1.clone();
        children2 = parent2.clone();
        children.add(children1);
        children.add(children2);
    }

    private void crossover() {
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            if (rand.nextDouble() < 0.5) {
                swapTwoGen(children1, children2, s, data.getLengthOfSubject()[s]);

            }
            if (rand.nextDouble() < 0.5) {
                swapTwoGenOnlyInvi(children1, children2, s, data.getLengthOfSubject()[s]);
            }
//            if (rand.nextDouble() < crossoverRate) {
//                swapTwoGenOnlyInvi(children1, children2, s, data.getLengthOfSubject()[s]);
//            }
        }
    }

    private void swapTwoGenOnlyInvi(Individual c1, Individual c2, int s, int sLength) {
        for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
            int[] temp = c1.getChromosome()[s][t];
            c1.getChromosome()[s][t] = c2.getChromosome()[s][t];
            c2.getChromosome()[s][t] = temp;
        }
    }

    private void swapTwoGen(Individual c1, Individual c2, int s, int sLength) {
        swapTwoGen(c1.getChromosome(), c2.getChromosome(), s, sLength, c1.getSubjectHeldAtSlot(), c1.getSlotStartOfSubject(), c2.getSubjectHeldAtSlot(), c2.getSlotStartOfSubject());
    }

    private void swapTwoGen(int[][][] child1, int[][][] child2, int s, int sLength, int[][] subjectHeldAtSlot1, int[] slotStart1, int[][] subjectHeldAtSlot2, int[] slotStart2) {
        int[][] temp = child1[s];
        child1[s] = child2[s];
        child2[s] = temp;
        for (int t = slotStart1[s]; t < slotStart1[s] + sLength; t++) {
            subjectHeldAtSlot1[s][t] = 0;
            subjectHeldAtSlot2[s][t] = 1;
        }
        for (int t = slotStart2[s]; t < slotStart2[s] + sLength; t++) {
            subjectHeldAtSlot1[s][t] = 1;
            subjectHeldAtSlot2[s][t] = 0;
        }
        int temp1 = slotStart1[s];
        slotStart1[s] = slotStart2[s];
        slotStart2[s] = temp1;
    }

    private void mutation() {
        mutationElement(children1);
        mutationElement(children2);
    }

    private void mutationElement(Individual ind) {
        int subjectIndex1 = rand.nextInt(data.getNumberOfSubjects());
        int subjectIndex2 = rand.nextInt(data.getNumberOfSubjects());
        while (subjectIndex1 == subjectIndex2) {
            subjectIndex2 = rand.nextInt(data.getNumberOfSubjects());
        }
        int lengthS1 = data.getLengthOfSubject()[subjectIndex1];
        int lengthS2 = data.getLengthOfSubject()[subjectIndex2];
//        Individual indClone = ind.clone();
        if (rand.nextDouble() < mutationRate) {
            swapTimeslotOfTwoSubject(ind.getChromosome(), subjectIndex1, subjectIndex2, ind.getSubjectHeldAtSlot(), ind.getSlotStartOfSubject(), lengthS1, lengthS2);
        } else {
            swapTwoInvi(ind.getChromosome(), subjectIndex1, subjectIndex2, ind.getSubjectHeldAtSlot(), ind.getSlotStartOfSubject(), lengthS1, lengthS2);
        }
//        if (indClone.passAllConstraints(indClone.chromosome)) {
//            ind = indClone;
//        }
    }

    private void swapTwoInvi(int[][][] mutatedChromosome, int subjectIndex1, int subjectIndex2, int[][] subjectHeldAtSlot, int[] slotStartOfSubject, int lengthS1, int lengthS2) {
        int slotStart1 = slotStartOfSubject[subjectIndex1];
        int slotStart2 = slotStartOfSubject[subjectIndex2];
        List<Integer> inviSubject1 = new ArrayList<>();
        List<Integer> inviSubject2 = new ArrayList<>();
        for (int i = 0; i < mutatedChromosome[0][0].length; i++) {
            if (mutatedChromosome[subjectIndex1][slotStart1][i] == 1) {
                inviSubject1.add(i);
            }
            if (mutatedChromosome[subjectIndex2][slotStart2][i] == 1) {
                inviSubject2.add(i);
            }

        }
        int invi1 = -1;
        int invi2 = -1;

        for (int i = 0; i < mutatedChromosome[0][0].length; i++) {
            if (invi1 == -1 && inviSubject1.contains(i) && !inviSubject2.contains(i) & data.getSubjectCanBeSuperviseInvigilator()[subjectIndex2][i] == 1) {
                invi1 = i;
            }
            if (invi2 == -1 && !inviSubject1.contains(i) && inviSubject2.contains(i) && data.getSubjectCanBeSuperviseInvigilator()[subjectIndex1][i] == 1) {
                invi2 = i;
            }
            if (invi1 != -1 && invi2 != -1) {
                break; // Break out of the loop if both invi1 and invi2 are found
            }
        }
        if (invi1 != -1 && invi2 != -1) {
            for (int t = slotStart1; t < slotStart1 + lengthS1; t++) {
                mutatedChromosome[subjectIndex1][t][invi1] = 0;
                mutatedChromosome[subjectIndex1][t][invi2] = 1;
            }

            for (int t = slotStart2; t < slotStart2 + lengthS2; t++) {
                mutatedChromosome[subjectIndex2][t][invi1] = 1;
                mutatedChromosome[subjectIndex2][t][invi2] = 0;
            }
        }
    }

    private void swapTimeslotOfTwoSubject(int[][][] mutatedChromosome, int subjectIndex1, int subjectIndex2, int[][] subHeldAtSlotCopy, int[] slotStartCopy, int lengthS1, int lengthS2) {
        if (slotStartCopy[subjectIndex1] == slotStartCopy[subjectIndex2]) {
            return;
        }
        if (slotStartCopy[subjectIndex1] + lengthS2 >= mutatedChromosome[0].length || slotStartCopy[subjectIndex2] + lengthS1 >= mutatedChromosome[0].length) {
            return;
        }
        List<Integer> inviSubject1 = new ArrayList<>();
        List<Integer> inviSubject2 = new ArrayList<>();
        for (int i = 0; i < mutatedChromosome[0][0].length; i++) {
            for (int t = slotStartCopy[subjectIndex1]; t < slotStartCopy[subjectIndex1] + lengthS1; t++) {
                if (mutatedChromosome[subjectIndex1][t][i] == 1) {
                    inviSubject1.add(i);
                    mutatedChromosome[subjectIndex1][t][i] = 0;
                    subHeldAtSlotCopy[subjectIndex1][t] = 0;
                }
            }
            for (int t = slotStartCopy[subjectIndex2]; t < slotStartCopy[subjectIndex2] + lengthS2; t++) {
                if (mutatedChromosome[subjectIndex2][t][i] == 1) {
                    inviSubject2.add(i);
                    mutatedChromosome[subjectIndex2][t][i] = 0;
                    subHeldAtSlotCopy[subjectIndex2][t] = 0;

                }
            }
        }
        for (int i = 0; i < mutatedChromosome[0][0].length; i++) {
            for (int t = slotStartCopy[subjectIndex1]; t < slotStartCopy[subjectIndex1] + lengthS2; t++) {
                if (inviSubject2.contains(i)) {
                    mutatedChromosome[subjectIndex2][t][i] = 1;
                    subHeldAtSlotCopy[subjectIndex2][t] = 1;
                }
            }
            for (int t = slotStartCopy[subjectIndex2]; t < slotStartCopy[subjectIndex2] + lengthS1; t++) {
                if (inviSubject1.contains(i)) {
                    mutatedChromosome[subjectIndex1][t][i] = 1;
                    subHeldAtSlotCopy[subjectIndex1][t] = 1;

                }
            }
        }
        int temp1 = slotStartCopy[subjectIndex1];
        slotStartCopy[subjectIndex1] = slotStartCopy[subjectIndex2];
        slotStartCopy[subjectIndex2] = temp1;
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
