package entities;

import com.opencsv.exceptions.CsvValidationException;
import dataInput.GeneticData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import myThread.FitnessCalculateThread;
import utils.DistributedRandom;

public class Individual implements Comparable<Individual> {

    private GeneticData getData;
    private int[][][] chromosome;
    private int[][] subjectHeldAtSlot;
    private int[] slotStartOfSubject;
    private double fitness;

    public Individual(GeneticData getData, int[][][] chromosome, int[][] subjectHeldAtSlot, int[] slotStartOfSubject, double fitness) {
        this.getData = getData;
        this.chromosome = chromosome;
        this.subjectHeldAtSlot = subjectHeldAtSlot;
        this.slotStartOfSubject = slotStartOfSubject;
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public Individual(GeneticData data) {
        this.getData = data;
    }

    public GeneticData getData() {
        return getData;
    }

    public void setData(GeneticData data) {
        this.getData = data;
    }

    public int[][][] getChromosome() {
        return chromosome;
    }

    public void setChromosome(int[][][] chromosome) {
        this.chromosome = chromosome;
    }

    public int[][] getSubjectHeldAtSlot() {
        return subjectHeldAtSlot;
    }

    public void setSubjectHeldAtSlot(int[][] subjectHeldAtSlot) {
        this.subjectHeldAtSlot = subjectHeldAtSlot;
    }

    public int[] getSlotStartOfSubject() {
        return slotStartOfSubject;
    }

    public void setSlotStartOfSubject(int[] slotStartOfSubject) {
        this.slotStartOfSubject = slotStartOfSubject;
    }

    public Individual(GeneticData data, int[][][] chromosome, int[][] subjectHeldAtSlot, int[] slotStartOfSubject, float fitness) {
        this.getData = data;
        this.chromosome = chromosome;
        this.subjectHeldAtSlot = subjectHeldAtSlot;
        this.slotStartOfSubject = slotStartOfSubject;
        this.fitness = fitness;
    }

    //Constraint 1: No student should be required to sit two examinations simultaneously
    public boolean checkNoSimultaneousExams() {
        for (int m = 0; m < getData.getNumberOfStudents(); m++) {
            for (int t = 0; t < getData.getNumberOfTotalSlots(); t++) {
                int subjectCounts = 0;
                for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
                    if (subjectHeldAtSlot[s][t] == 1
                            && getData.getStudentTakeSubject()[m][s] == 1) {
                        subjectCounts += 1;
                    }
                }
                if (subjectCounts > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    //Constraint 2: In each slot, the number of invigilator must be equal to number of room required for each subject
    public boolean checkInvigilatorRoomMatch() {
        for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
            for (int t = 0; t < getData.getNumberOfTotalSlots(); t++) {
                int invigilatorCounts = 0;
                for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                    invigilatorCounts += chromosome[s][t][i];
                }
                if (invigilatorCounts != 0 && invigilatorCounts != getData.getNumberOfRoomsOfEachSubject()[s]) {
                    return false;
                }
            }
        }
        return true;
    }

    //Constraint 3: No invigilator should be required to sit two examinations simultaneously
    public boolean checkNoInvigilatorClashes() {
        for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
            for (int t = 0; t < getData.getNumberOfTotalSlots(); t++) {
                int subjectCounts = 0;
                for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
                    subjectCounts += chromosome[s][t][i];
                }
                if (subjectCounts > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    //Constraint 4: Invigilator is scheduled to subject that belong to their capacity
    public boolean checkInvigilatorCapacity() {
        for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
            for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                for (int t = 0; t < getData.getNumberOfTotalSlots(); t++) {
                    if (chromosome[s][t][i] == 1 && getData.getSubjectCanBeSuperviseInvigilator()[s][i] == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //Constraint 5: Number of rooms used in one slot is not larger than university's capacity
    public boolean checkRoomCapacity() {
        for (int t = 0; t < getData.getNumberOfTotalSlots(); t++) {
            int roomCounts = 0;
            for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
                for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                    roomCounts += chromosome[s][t][i];
                }
            }
            if (roomCounts > getData.getNumberOfRooms()) {
                return false;
            }
        }

        return true;
    }

    //Constraint 6: One subject only take place at one time
    public boolean checkSingleSubjectAtATime() {
        for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
            int sum = 0;
            for (int t = 0; t < getData.getNumberOfTotalSlots(); t++) {
                sum += subjectHeldAtSlot[s][t];
            }
            if (sum != getData.getLengthOfSubject()[s]) {
                return false;
            }
        }
        return true;
    }

    //Constraint 7: All subject must happen only one part of day (in the morning or in the afternoon)
    public boolean checkSubjectPartOfDay() {
        int startOfSubject;
        int endOfSubject;

        for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
            startOfSubject = (int) (2.0 * slotStartOfSubject[s] / getData.getNumberOfSlotsPerDay());
            endOfSubject = (int) (2.0 * (slotStartOfSubject[s] + getData.getLengthOfSubject()[s] - 1) / getData.getNumberOfSlotsPerDay());
            if (startOfSubject != endOfSubject) {
                return false;
            }

        }
        return true;
    }

    //Constraint 8: With each subject, Invigilator need to supervise all consecutive slot of this subject happen.
    public boolean checkInvigilatorConsecutiveSlots() {
        for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
            for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                int consecutiveCount = 0;
                int startSlot = slotStartOfSubject[s];
                int endSlot = slotStartOfSubject[s] + getData.getLengthOfSubject()[s] - 1;
                for (int t = startSlot; t <= endSlot; t++) {
                    consecutiveCount += chromosome[s][t][i];
                }
                if (consecutiveCount != 0 && consecutiveCount != getData.getLengthOfSubject()[s]) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean passAllConstraints() {
        return checkNoSimultaneousExams()
                && checkInvigilatorRoomMatch()
                && checkNoInvigilatorClashes()
                && checkInvigilatorCapacity()
                && checkRoomCapacity()
                && checkSingleSubjectAtATime()
                && checkSubjectPartOfDay()
                && checkInvigilatorConsecutiveSlots();
    }

    public void createChromosome() {
        while (true) {
            chromosome = new int[getData.getNumberOfSubjects()][getData.getNumberOfTotalSlots()][getData.getNumberOfInvigilators()];
            int[] numberOfSlotScheduleInvigilator = new int[getData.getNumberOfInvigilators()];
            int[][] invigilatorTakeSlot = new int[getData.getNumberOfInvigilators()][getData.getNumberOfTotalSlots()];
            subjectHeldAtSlot = new int[getData.getNumberOfSubjects()][getData.getNumberOfTotalSlots()];
            slotStartOfSubject = new int[getData.getNumberOfSubjects()];
            DistributedRandom randomSlot = DistributedRandom.newDistributedRandomSlot(getData.numberOfTotalSlots, getData.numberOfRooms);
//            Random rand = new Random();

            for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                numberOfSlotScheduleInvigilator[i] = getData.getNumberOfSlotsRequiredForInvigilators()[i];
            }

            for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
//                System.out.println("s: "+ s);
                int sLength = getData.getLengthOfSubject()[s];
                int[] haveChoosen = new int[getData.getNumberOfTotalSlots()];
                int t = randomSlot.getRandom();
//                int t = rand.nextInt(getData.getNumberOfTotalSlots() - sLength+1);
                boolean flag = false;
                while (true) {
                    int sum = 0;
                    haveChoosen[t] = 1;
                    for (int index = 0; index < haveChoosen.length; index++) {
                        sum += haveChoosen[index];
                    }
                    if (sum == getData.getNumberOfTotalSlots() - sLength + 1) {
                        flag = true;
                        break;
                    }

                    if (checkSubjectFitSlot(sLength, t, getData.getNumberOfSlotsPerDay())
                            && checkSubjectOverlapSlot(s, t, sLength, getData.getNumberOfSubjects(), subjectHeldAtSlot, getData.getOverlapSubject())
                            && checkRoomCapacityAtOneTime(chromosome, t, s, sLength)) {
                        break;
                    }
                    t = randomSlot.getRandom();
//                    t = rand.nextInt(getData.getNumberOfTotalSlots() - sLength + 1);
                }
                if (flag == true) {
//                    System.out.println("Can't choose t");
                    break;
                }

                DistributedRandom randomInvigilator = new DistributedRandom();
                int countInvigilator = 0;
                int invigilatorNeed = getData.getNumberOfRoomsOfEachSubject()[s];

                for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                    boolean canAdd = true;
                    for (int eachSlot = t; eachSlot < t + sLength; eachSlot++) {
                        if (getData.getSubjectCanBeSuperviseInvigilator()[s][i] == 1 && invigilatorTakeSlot[i][eachSlot] == 0 && numberOfSlotScheduleInvigilator[i] > 0) {
                            canAdd = true;
                        } else {
                            canAdd = false;
                            break;
                        }
                    }

                    if (canAdd) {
                        randomInvigilator.add(i, numberOfSlotScheduleInvigilator[i]);
                        countInvigilator++;
                    }
                }

                if (countInvigilator < invigilatorNeed) {
                    System.out.println("Not enough invigilators: " + s);
                }

                for (int i = 0; i < invigilatorNeed; i++) {
                    int currentInvigilator = randomInvigilator.getRandom();

                    if (getData.getSubjectCanBeSuperviseInvigilator()[s][currentInvigilator] != 1) {
                        System.out.println("Invalid invigilator assigned for subject");
                    }

                    for (int eachSlot = t; eachSlot < t + sLength; eachSlot++) {
                        if (invigilatorTakeSlot[currentInvigilator][eachSlot] != 0) {
                            System.out.println("Invalid slot assignment for invigilator: " + currentInvigilator);
                        }

                        chromosome[s][eachSlot][currentInvigilator] = 1;
                        invigilatorTakeSlot[currentInvigilator][eachSlot] = 1;
                        subjectHeldAtSlot[s][eachSlot] = 1;
                        randomSlot.add(eachSlot, -1);
                    }

                    randomInvigilator.delete(currentInvigilator);
                    numberOfSlotScheduleInvigilator[currentInvigilator]--;
                }
                slotStartOfSubject[s] = t;
            }
            if (passAllConstraints()) {
                break;
            }
        }
    }

    private boolean checkRoomCapacityAtOneTime(int[][][] chromosome, int t, int s, int sLength) {
        boolean flag = true;
        if (t + sLength > getData.getNumberOfTotalSlots() - 1) {
            return false;
        }
        for (int eachSlot = t; eachSlot < t + sLength; eachSlot++) {
            int roomCounts = 0;
            for (int k = 0; k < s; k++) {
                for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                    roomCounts += chromosome[k][eachSlot][i];
                }
            }
            if (getData.getNumberOfRooms() - roomCounts - getData.getNumberOfRoomsOfEachSubject()[s] < 0) {
                flag = false;
            }
        }

        return flag;
    }

    private boolean checkSubjectFitSlot(int subLength, int slot, int slotsPerDay) {
        double half = slotsPerDay / 2;
        return (int) (slot / half) == (int) ((slot + subLength - 1) / half);
    }

    private boolean checkSubjectOverlapSlot(int currentSubject, int currentTime, int subjectLength, int numberOfSubjects, int[][] subjectTakeSlot, int[][] overlapSubject) {
        for (int slot = currentTime; slot < currentTime + subjectLength; slot++) {
            for (int preSubject = 0; preSubject < numberOfSubjects; preSubject++) {
                if (preSubject == currentSubject || overlapSubject[currentSubject][preSubject] == 0) {
                    continue;
                }
                if (subjectTakeSlot[preSubject][slot] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][][] readChromosomeFromFile(String filePath) {
        int[][][] chromosome = new int[getData.getNumberOfSubjects()][getData.getNumberOfTotalSlots()][getData.getNumberOfInvigilators()];
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            while (line != null) {
                // Extract subject, slot, invigilator, and value from each line
                String[] parts = line.split(":")[0].trim().split(",");
                int subject = Integer.parseInt(parts[0].trim().split(" ")[1]);
                int slot = Integer.parseInt(parts[1].trim().split(" ")[1]);
                int invigilator = Integer.parseInt(parts[2].trim().split(" ")[1]);
                int value = Integer.parseInt(line.split(":")[1].trim());
                chromosome[subject][slot][invigilator] = value;
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chromosome;
    }

    public void writeChromosomeToFile(String filePath, int[][][] chromosome) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        for (int s = 0; s < getData.getNumberOfSubjects(); s++) {
            for (int t = 0; t < getData.getNumberOfTotalSlots(); t++) {
                for (int i = 0; i < getData.getNumberOfInvigilators(); i++) {
                    String line = String.format("Subject %d, Slot %d, Invigilator %d: %d\n", s, t, i, chromosome[s][t][i]);
                    writer.write(line);
                }
            }
        }
        writer.close();
    }

    public Individual clone() {
        int[][][] newChromosome = Arrays.stream(chromosome).map(row -> Arrays.stream(row).map(int[]::clone).toArray(int[][]::new)).toArray(int[][][]::new);
        int[][] newSubjectHeldAtSlot = Arrays.stream(subjectHeldAtSlot).map(int[]::clone).toArray(int[][]::new);
        int[] newSlotStartOfSubject = Arrays.copyOf(slotStartOfSubject, slotStartOfSubject.length);
        double fitness = this.fitness;
        Individual newIndividual = new Individual(getData, newChromosome, newSubjectHeldAtSlot, newSlotStartOfSubject, fitness);
        return newIndividual;
    }

    @Override
    public int compareTo(Individual o) {
        if (fitness == o.getFitness()) {
            return 0;
        } else {
            if (fitness > o.getFitness()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, CsvValidationException {
        GeneticData data = new GeneticData();
        data.loadData();
        Individual id = new Individual(data);
        String filePath = "data/output.txt";
//        int[][][] chromosome = s.readChromosomeFromFile(filePath);
////        System.out.println(chromosome[1][30][259]);
//        id.createChromosome();
        id.setChromosome(id.readChromosomeFromFile(filePath));
        id.generateInfo(id);
        if (id.passAllConstraints()) {
            System.out.println("Chromosome passes all constraints.");
        } else {
            System.out.println("Chromosome does not pass all constraints.");
        }
//        id.setChromosome(id.readChromosomeFromFile(filePath));
//        id.generateInfo(id);
//        FitnessCalculateThread fc = new FitnessCalculateThread(id);
//        fc.setIndividual(id);
//        fc.calculateFitess();
//        System.out.println(fc.calculateFitess());
        // System.out.println(data.invigilatorCanSuperviseSubject[274][4]);

    }

    public void generateInfo(Individual ind) {
        int[][][] chro = ind.getChromosome();
        createSubjectHeldAtSlot(ind);
        createSlotStartOfSubject(ind);
    }

    public void createSubjectHeldAtSlot(Individual ind) {
        int[][][] chromosome = ind.getChromosome();
        int[][] subjectHeldAtSlot = new int[ind.getData().getNumberOfSubjects()][ind.getData().getNumberOfTotalSlots()];
        for (int s = 0; s < ind.getData().getNumberOfSubjects(); s++) {
            for (int t = 0; t < ind.getData().getNumberOfTotalSlots(); t++) {
                int numberOfInvigilators = 0;
                for (int i = 0; i < ind.getData().getNumberOfInvigilators(); i++) {
                    numberOfInvigilators += chromosome[s][t][i];
                }
                if (numberOfInvigilators > 0) {
                    subjectHeldAtSlot[s][t] = 1;
                }
            }
        }
        ind.setSubjectHeldAtSlot(subjectHeldAtSlot);
    }

    public void createSlotStartOfSubject(Individual ind) {
        int[] slotStartOfSubject = new int[ind.getData().getNumberOfSubjects()];
        for (int s = 0; s < ind.getData().getNumberOfSubjects(); s++) {
            for (int t = 1; t < ind.getData().getNumberOfTotalSlots(); t++) {
                if (ind.getSubjectHeldAtSlot()[s][t - 1] == 0 && ind.getSubjectHeldAtSlot()[s][t] == 1) {
                    slotStartOfSubject[s] = t;
                    break; // Assuming only one start per subject
                }
            }
        }
        ind.setSlotStartOfSubject(slotStartOfSubject);
    }

    public static void print(int[][][] ch) {
        for (int i = 0; i < ch.length; i++) {
            for (int j = 0; j < ch[0].length; j++) {
                for (int t = 0; t < ch[0][0].length; t++) {
                    if (ch[i][j][t] == 1) {
                        System.out.println(i + " " + j + " " + t);
                        return;
                    }
                }
            }
        }
    }
}
