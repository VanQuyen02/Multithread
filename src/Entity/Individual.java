package Entity;

import Data.GeneticData;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import utils.DistributedRandom;

public class Individual implements Comparable<Individual> {

    public float fitness;
    public GeneticData data;
    public int[][][] chromosome;

    public GeneticData getData() {
        return data;
    }

    public void setData(GeneticData data) {
        this.data = data;
    }

    public int[][][] getChromosome() {
        return chromosome;
    }

    public void setChromosome(int[][][] chromosome) {
        this.chromosome = chromosome;
    }

    public Individual() {
    }

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    public Individual clone() {
        Individual newIndividual = new Individual();
        newIndividual.setData(data);
        newIndividual.setChromosome(chromosome);
        newIndividual.setFitness(fitness);
        return newIndividual;
    }

    @Override
    public int compareTo(Individual o) {
        if (fitness == o.getFitness()) {
            return 0;
        } else {
            if (fitness > o.fitness) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    //Constraint 1: No student should be required to sit two examinations simultaneously
    public boolean checkNoSimultaneousExams(int[][][] chromosome) {
        int[][] subjectHeldAtSlot = createSubjectHeldAtSlot(chromosome);
        for (int m = 0; m < data.getNumberOfStudents(); m++) {
            for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                int subjectCounts = 0;
                for (int s = 0; s < data.getNumberOfSubjects(); s++) {
                    if (subjectHeldAtSlot[s][t] == 1
                            && data.getStudentTakeSubject()[m][s] == 1) {
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
    public boolean checkInvigilatorRoomMatch(int[][][] chromosome) {
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                int invigilatorCounts = 0;
                for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                    invigilatorCounts += chromosome[s][t][i];
                }
                if (invigilatorCounts != 0 && invigilatorCounts != data.getNumberOfRoomsOfEachSubject()[s]) {
//                    System.out.println(invigilatorCounts);
//                    System.out.println(data.getNumberOfRoomsOfEachSubject()[s]);
                    return false;
                }
            }
        }
        return true;
    }

    //Constraint 3: No invigilator should be required to sit two examinations simultaneously
    public boolean checkNoInvigilatorClashes(int[][][] chromosome) {
        for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
            for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                int subjectCounts = 0;
                for (int s = 0; s < data.getNumberOfSubjects(); s++) {
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
    public boolean checkInvigilatorCapacity(int[][][] chromosome) {
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                    if (chromosome[s][t][i] == 1 && data.getSubjectCanBeSuperviseInvigilator()[s][i] == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //Constraint 5: Number of rooms used in one slot is not larger than university's capacity
    public boolean checkRoomCapacity(int[][][] chromosome) {
        for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
            int roomCounts = 0;
            for (int s = 0; s < data.getNumberOfSubjects(); s++) {
                for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                    roomCounts += chromosome[s][t][i];
                }
            }
            if (roomCounts > data.getNumberOfRooms()) {
                return false;
            }
        }

        return true;
    }

    //Constraint 6: One subject only take place at one time
    public boolean checkSingleSubjectAtATime(int[][][] chromosome) {
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            ArrayList<Integer> cnt = new ArrayList<>();
            for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                int numberOfInvigilators = 0;
                for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                    numberOfInvigilators += chromosome[s][t][i];
                }
                if (numberOfInvigilators > 0) {
                    cnt.add(t);
                }
            }
            if (cnt.size() != data.getLengthOfSubject()[s]) {
                return false;
            }
        }
        return true;
    }

    //Constraint 7: All subject must happen only one part of day (in the morning or in the afternoon)
    public boolean checkSubjectPartOfDay(int[][][] chromosome) {
        int[][] subjectHeldAtSlot = createSubjectHeldAtSlot(chromosome);
        int[][] slotStartOfSubject = createSlotStartOfSubject(subjectHeldAtSlot);
        int[][] slotEndOfSubject = createSlotEndOfSubject(slotStartOfSubject);

        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            int[] startOfSubject = new int[slotStartOfSubject[s].length];
            int[] endOfSubject = new int[slotEndOfSubject[s].length];

            for (int i = 0; i < slotStartOfSubject[s].length; i++) {
                startOfSubject[i] = (int) (2.0 * slotStartOfSubject[s][i] / data.getNumberOfSlotsPerDay());
                endOfSubject[i] = (int) (2.0 * slotEndOfSubject[s][i] / data.getNumberOfSlotsPerDay());
            }

            for (int i = 0; i < startOfSubject.length; i++) {
                if (Math.floor(startOfSubject[i]) != Math.floor(endOfSubject[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    //Constraint 8: With each subject, Invigilator need to supervise all consecutive slot of this subject happen.
    public boolean checkInvigilatorConsecutiveSlots(int[][][] chromosome) {
        int[][] subjectHeldAtSlot = createSubjectHeldAtSlot(chromosome);
        int[][] slotStartOfSubject = createSlotStartOfSubject(subjectHeldAtSlot);
        int[][] slotEndOfSubject = createSlotEndOfSubject(slotStartOfSubject);

        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                int consecutiveCount = 0;
                int startSlot = slotStartOfSubject[s][0];
                int endSlot = slotEndOfSubject[s][0];
                for (int t = startSlot; t <= endSlot; t++) {
                    consecutiveCount += chromosome[s][t][i];
                }
                if (consecutiveCount != 0 && consecutiveCount != data.getLengthOfSubject()[s]) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] createSubjectHeldAtSlot(int[][][] chromosome) {
        int[][] subjectHeldAtSlot = new int[data.getNumberOfSubjects()][data.getNumberOfTotalSlots()];
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                int numberOfInvigilators = 0;
                for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                    numberOfInvigilators += chromosome[s][t][i];
                }
                if (numberOfInvigilators > 0) {
                    subjectHeldAtSlot[s][t] = 1;
                }
            }
        }
        return subjectHeldAtSlot;
    }

    public int[][] createSlotStartOfSubject(int[][] subjectHeldAtSlot) {
        int[][] slotStartOfSubject = new int[data.getNumberOfSubjects()][1];
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            for (int t = 1; t < data.getNumberOfTotalSlots(); t++) {
                if (subjectHeldAtSlot[s][t - 1] == 0 && subjectHeldAtSlot[s][t] == 1) {
                    slotStartOfSubject[s][0] = t;
                    break; // Assuming only one start per subject
                }
            }
        }
        return slotStartOfSubject;
    }

    public int[][] createSlotEndOfSubject(int[][] slotStartOfSubject) {
        int[][] slotEndOfSubject = new int[data.getNumberOfSubjects()][1];
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            slotEndOfSubject[s][0] = slotStartOfSubject[s][0] + data.getLengthOfSubject()[s] - 1;
        }
        return slotEndOfSubject;
    }

    public boolean passAllConstraints(int[][][] chromosome) {
        boolean constraint1 = checkNoSimultaneousExams(chromosome);
        boolean constraint2 = checkInvigilatorRoomMatch(chromosome);
        boolean constraint3 = checkNoInvigilatorClashes(chromosome);
        boolean constraint4 = checkInvigilatorCapacity(chromosome);
        boolean constraint5 = checkRoomCapacity(chromosome);
        boolean constraint6 = checkSingleSubjectAtATime(chromosome);
        boolean constraint7 = checkSubjectPartOfDay(chromosome);
        boolean constraint8 = checkInvigilatorConsecutiveSlots(chromosome);

//        System.out.println(constraint1 + " " + constraint2 + " " + constraint3 + " " + constraint4 + " " + constraint5 + " " + constraint6 + " " + constraint7 + " " + constraint8);
        return constraint1 && constraint2 && constraint3 && constraint4 && constraint5 && constraint6 && constraint7 && constraint8;
    }

    public int[][][] createChromosome() {
        while (true) {
            int[][][] chromosome = new int[data.getNumberOfSubjects()][data.getNumberOfTotalSlots()][data.getNumberOfInvigilators()];
            int[] numberOfSlotScheduleInvigilator = new int[data.getNumberOfInvigilators()];
            int[][] invigilatorTakeSlot = new int[data.getNumberOfInvigilators()][data.getNumberOfTotalSlots()];
            int[][] subjectHeldAtSlot = new int[data.getNumberOfSubjects()][data.getNumberOfTotalSlots()];
//            DistributedRandom randomSlot = DistributedRandom.newDistributedRandomSlot(data.numberOfTotalSlots, data.numberOfRooms);
            Random rand = new Random();

            for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                numberOfSlotScheduleInvigilator[i] = data.getNumberOfSlotsRequiredForInvigilators()[i];
            }

            for (int s = 0; s < data.getNumberOfSubjects(); s++) {
//                System.out.println("s: "+ s);
                int sLength = data.getLengthOfSubject()[s];
                int[] haveChoosen = new int[data.getNumberOfTotalSlots() - sLength];
//                int t = randomSlot.getRandom();
                int t = rand.nextInt(data.getNumberOfTotalSlots() - sLength);
                boolean flag = false;
                while (true) {
                    int sum = 0;
                    haveChoosen[t] = 1;
                    for (int index = 0; index < haveChoosen.length; index++) {
                        sum += haveChoosen[index];
                    }
                    if (sum == data.getNumberOfTotalSlots() - sLength) {
                        flag = true;
                        break;
                    }

                    if (checkSubjectFitSlot(sLength, t, data.getNumberOfSlotsPerDay())
                            && checkSubjectOverlapSlot(s, t, sLength, data.getNumberOfSubjects(), subjectHeldAtSlot, data.getOverlapSubject())
                            && checkRoomCapacityAtOneTime(chromosome, t, s, sLength)) {
                        break;
                    }
                    t = rand.nextInt(data.getNumberOfTotalSlots() - sLength);
                }
                if (flag == true) {
//                    System.out.println("Can't choose t");
                    break;
                }

                DistributedRandom randomInvigilator = new DistributedRandom();
                int countInvigilator = 0;
                int invigilatorNeed = data.getNumberOfRoomsOfEachSubject()[s];

                for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                    boolean canAdd = true;
                    for (int eachSlot = t; eachSlot < t + sLength; eachSlot++) {
                        if (data.getSubjectCanBeSuperviseInvigilator()[s][i] == 1 && invigilatorTakeSlot[i][eachSlot] == 0 && numberOfSlotScheduleInvigilator[i] > 0) {
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

                    if (data.getSubjectCanBeSuperviseInvigilator()[s][currentInvigilator] != 1) {
                        System.out.println("Invalid invigilator assigned for subject");
                    }

                    for (int eachSlot = t; eachSlot < t + sLength; eachSlot++) {
                        if (invigilatorTakeSlot[currentInvigilator][eachSlot] != 0) {
                            System.out.println("Invalid slot assignment for invigilator: " + currentInvigilator);
                        }

                        chromosome[s][eachSlot][currentInvigilator] = 1;
                        invigilatorTakeSlot[currentInvigilator][eachSlot] = 1;
                        subjectHeldAtSlot[s][eachSlot] = 1;
//                        randomSlot.add(eachSlot, -1);
                    }

                    randomInvigilator.delete(currentInvigilator);
                    numberOfSlotScheduleInvigilator[currentInvigilator]--;
                }
            }
            if (passAllConstraints(chromosome)) {
                return chromosome;
            }
        }

    }

    private boolean checkRoomCapacityAtOneTime(int[][][] chromosome, int t, int s, int sLength) {
        boolean flag = true;
        if (t + sLength > data.numberOfTotalSlots - 1) {
            return false;
        }
        for (int eachSlot = t; eachSlot < t + sLength; eachSlot++) {
            int roomCounts = 0;
            for (int k = 0; k < s; k++) {
                for (int i = 0; i < data.numberOfInvigilators; i++) {
                    roomCounts += chromosome[k][eachSlot][i];
                }
            }
            if (data.numberOfRooms - roomCounts - data.numberOfRoomsOfEachSubject[s] < 0) {
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
        int[][][] chromosome = new int[data.getNumberOfSubjects()][data.getNumberOfTotalSlots()][data.getNumberOfInvigilators()];
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            while (line != null) {
                // Extract subject, slot, invigilator, and value from each line
                String[] parts = line.split(":")[0].trim().split(",");
                int subject = Integer.parseInt(parts[0].split(" ")[1]);
                int slot = Integer.parseInt(parts[1].split(" ")[2]);
                int invigilator = Integer.parseInt(parts[2].split(" ")[2]);
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

    public void testChromosomeFromFile(String filePath) throws IOException {
        int[][][] chromosome = readChromosomeFromFile(filePath);

        if (passAllConstraints(chromosome)) {
            System.out.println("Chromosome passes all constraints.");
        } else {
            System.out.println("Chromosome does not pass all constraints.");
        }
    }

    public void writeChromosomeToFile(String filePath, int[][][] chromosome) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        for (int s = 0; s < data.getNumberOfSubjects(); s++) {
            for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
                    String line = String.format("Subject %d, Slot %d, Invigilator %d: %d\n", s, t, i, chromosome[s][t][i]);
                    writer.write(line);
                }
            }
        }
        writer.close();
    }

    public static void main(String[] args) {
        Individual p = new Individual();
        int[][][] chromosome = new int[3][3][3];
        p.setChromosome(chromosome);
        System.out.println(p.chromosome[0][0][0]);
        Individual q = p.clone();
        int[][][] chromosome1 = p.getChromosome().clone();
        chromosome1[0][0][0]=1;
        System.out.println(q.getChromosome()[0][0][0]);
    }
}
