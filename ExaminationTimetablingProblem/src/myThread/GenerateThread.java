package myThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import dataInput.GeneticData;
import entities.Individual;

public class GenerateThread implements Runnable {

    private GeneticData data;
    private int generation;

    private Individual parent1;
    private Individual parent2;
    private Individual children1;
    private Individual children2;
    private BlockingBuffer newPopulation;
    private ArrayList<Individual> children;
    private double mutationRate;
    private double crossoverRate;
    Random rand = new Random();

    public GenerateThread(BlockingBuffer newPopulation, GeneticData geneticData, ArrayList<Individual> population, double crossoverRate, double mutationRate, int generation) {
        this.data = geneticData;
        this.newPopulation = newPopulation;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.generation = generation;
        Collections.shuffle(population);
        parent1 = population.get(0);
        parent2 = population.get(1);

        children = new ArrayList<>();
        children1 = parent1.clone();
        children2 = parent2.clone();
        children.add(children1);
//        children.add(children2);
    }

    private void crossover() {
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            if (rand.nextDouble() < 0.5) {
                swapTwoGen(children1, children2, s, data.getLengthOfSubject()[s]);
            }
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
//        mutationElement(children2);
    }

    private void mutation1() {
        mutationElement1(children1);
//        mutationElement1(children2);
    }

    private void mutation2() {
        mutationElement2(children1);
//        mutationElement2(children2);
    }

    private void mutation3() {
        mutationElement3(children1);
//        mutationElement3(children2);
    }

    private void mutationElement(Individual ind) {
        int subjectIndex1 = rand.nextInt(data.getNumberOfSubjects());
        int subjectIndex2 = rand.nextInt(data.getNumberOfSubjects());
        while (subjectIndex1 == subjectIndex2) {
            subjectIndex2 = rand.nextInt(data.getNumberOfSubjects());
        }
        int lengthS1 = data.getLengthOfSubject()[subjectIndex1];
        int lengthS2 = data.getLengthOfSubject()[subjectIndex2];
        swapTimeslotOfTwoSubject(ind.getChromosome(), subjectIndex1, subjectIndex2, ind.getSubjectHeldAtSlot(), ind.getSlotStartOfSubject(), lengthS1, lengthS2);
    }

    private void mutationElement1(Individual ind) {
        int random_subject = rand.nextInt(data.getNumberOfSubjects());
        changeInvi(ind, random_subject);
    }

    private void mutationElement2(Individual ind) {
        int subjectIndex1 = rand.nextInt(data.getNumberOfSubjects());
        int subjectIndex2 = rand.nextInt(data.getNumberOfSubjects());
        while (subjectIndex1 == subjectIndex2) {
            subjectIndex2 = rand.nextInt(data.getNumberOfSubjects());
        }
        int lengthS1 = data.getLengthOfSubject()[subjectIndex1];
        int lengthS2 = data.getLengthOfSubject()[subjectIndex2];
        swapTwoInvi(ind.getChromosome(), subjectIndex1, subjectIndex2, ind.getSubjectHeldAtSlot(), ind.getSlotStartOfSubject(), lengthS1, lengthS2);
    }

    private void mutationElement3(Individual ind) {
        int random_subject = rand.nextInt(data.getNumberOfSubjects());
        changeSubject(ind, random_subject);
    }

    private void changeInvi(Individual indi, int random_subject) {
        int[][] subjectHeldAtSlot = indi.getSubjectHeldAtSlot();
        int[] subjectStartdAtSlot = indi.getSlotStartOfSubject();
        int slotStart = subjectStartdAtSlot[random_subject];
        int lengthS = indi.getData().getLengthOfSubject()[random_subject];

        // Change slot of subject
//        int newSlotStart;
//        while (true) {
//            newSlotStart = rand.nextInt(indi.getData().getNumberOfTotalSlots() - lengthS);
//            if (newSlotStart != slotStart) {
//                break;
//            }
//        }
        List<Integer> invi = new ArrayList<>();
        for (int t = slotStart; t < slotStart + lengthS; t++) {
            for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                if (indi.getChromosome()[random_subject][t][i] == 1) {
//                    indi.getChromosome()[random_subject][t][i] = 0;
                    invi.add(i);
                }
            }
        }
        // Convert List to Set
        Set<Integer> set = new HashSet<>(invi);
        // Convert Set to List
        invi = new ArrayList<>(set);
//        for (int t = newSlotStart; t < newSlotStart + lengthS; t++) {
//            for (int index = 0; index < invi.size(); index++) {
//                indi.getChromosome()[random_subject][t][invi.get(index)] = 1;
//            }
//        }
//        for (int t = slotStart; t < slotStart + lengthS; t++) {
//            subjectHeldAtSlot[random_subject][t] = 0;
//        }
//        for (int t = newSlotStart; t < newSlotStart + lengthS; t++) {
//            subjectHeldAtSlot[random_subject][t] = 1;
//        }
//        subjectStartdAtSlot[random_subject] = newSlotStart;
//        indi.setSlotStartOfSubject(subjectStartdAtSlot);
//        indi.setSubjectHeldAtSlot(subjectHeldAtSlot);
        // Change Invi
        int newInvi;
        while (true) {
            newInvi = rand.nextInt(indi.getData().getNumberOfInvigilators());
            if (!invi.contains(newInvi) & indi.getData().getSubjectCanBeSuperviseInvigilator()[random_subject][newInvi] == 1) {
                break;
            }
        }
        int random_invigilator_index = rand.nextInt(invi.size());
        int random_invigilator = invi.get(random_invigilator_index);
        for (int t = slotStart; t < slotStart + lengthS; t++) {
            indi.getChromosome()[random_subject][t][newInvi] = 1;
            indi.getChromosome()[random_subject][t][random_invigilator] = 0;
        }
    }

    private void changeSubject(Individual indi, int random_subject) {
        int[][] subjectHeldAtSlot = indi.getSubjectHeldAtSlot();
        int[] subjectStartdAtSlot = indi.getSlotStartOfSubject();
        int slotStart = subjectStartdAtSlot[random_subject];
        int lengthS = indi.getData().getLengthOfSubject()[random_subject];

        // Change slot of subject
        int newSlotStart;
        while (true) {
            newSlotStart = rand.nextInt(indi.getData().getNumberOfTotalSlots() - lengthS + 1);
            if (newSlotStart != slotStart) {
                break;
            }
        }
        List<Integer> invi = new ArrayList<>();
        for (int t = slotStart; t < slotStart + lengthS; t++) {
            for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                if (indi.getChromosome()[random_subject][t][i] == 1) {
                    indi.getChromosome()[random_subject][t][i] = 0;
                    invi.add(i);
                }
            }
        }
        // Convert List to Set
        Set<Integer> set = new HashSet<>(invi);
        // Convert Set to List
        invi = new ArrayList<>(set);
        for (int t = newSlotStart; t < newSlotStart + lengthS; t++) {
            for (int index = 0; index < invi.size(); index++) {
                indi.getChromosome()[random_subject][t][invi.get(index)] = 1;
            }
        }
        for (int t = slotStart; t < slotStart + lengthS; t++) {
            subjectHeldAtSlot[random_subject][t] = 0;
        }
        for (int t = newSlotStart; t < newSlotStart + lengthS; t++) {
            subjectHeldAtSlot[random_subject][t] = 1;
        }
        subjectStartdAtSlot[random_subject] = newSlotStart;
        indi.setSlotStartOfSubject(subjectStartdAtSlot);
        indi.setSubjectHeldAtSlot(subjectHeldAtSlot);
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
            if (invi1 == -1 && inviSubject1.contains(i) && !inviSubject2.contains(i) && data.getSubjectCanBeSuperviseInvigilator()[subjectIndex2][i] == 1) {
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
//        if (generation < 1000) {
//            crossoverRate = 0.8;
//            mutationRate = 0.4;
//        } else {
//            crossoverRate = 0.4;
//            mutationRate = 0.8;
//        }
        //crossover
        if (Math.random() < crossoverRate) {
            crossover();
        }
        //mutation
        if (Math.random() < mutationRate) {
            double p = Math.random();
            if (p < 0.5) {
                mutation1();
                mutation3();
            } else {
                mutation();
                mutation2();
            }
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
