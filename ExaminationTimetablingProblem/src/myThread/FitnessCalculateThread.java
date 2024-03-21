package myThread;

import dataInput.GeneticData;
import java.util.Arrays;
import entities.Individual;

public class FitnessCalculateThread implements Runnable {

    private Individual individual;

    public FitnessCalculateThread(Individual individual) {
        this.individual = individual;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public float calculateFitess() {
        double w3 = 5 / 20.0;
        double w4 = 14 / 20.0;
        double w5 = 1 / 20.0;
        double epsilon = 1000;
//        double w3 = 1 / 4.0;
//        double w4 = 2.5 / 4.0;
//        double w5 = 1.5 / 4.0;
        double fitnessValue;
        double payoffStudent = calPayoffStudent(individual.getChromosome());
        double payoffInvigilator = calPayoffInvigilator(individual.getChromosome());
        double payoffP0 = calPayoffP0(individual.getChromosome());
//            System.out.println("payoff student: " + w3 * payoffStudent);
//            System.out.println("pay off invigiglator: " + w4 * payoffInvigilator);
//            System.out.println("pay off pdt: " + w5 * payoffP0);
        fitnessValue = w3 * payoffStudent + w4 * payoffInvigilator + w5 * payoffP0;
        if (individual.passAllConstraints()) {

        } else {
            fitnessValue *= epsilon;
        }
        return (float) fitnessValue;
    }

    public double calPayoffStudent(int[][][] chromosome) {
        int[] slotStartOfSubject = individual.getSlotStartOfSubject();
        int[][] slotStartOfStudent = new int[individual.getData().getNumberOfStudents()][individual.getData().getNumberOfSubjects()];
//        int[][] slotEndOfStudent = new int[individual.getData().getNumberOfStudents()][individual.getData().getNumberOfSubjects()];

        for (int m = 0; m < individual.getData().getNumberOfStudents(); m++) {
            for (int s = 0; s < individual.getData().getNumberOfSubjects(); s++) {
                slotStartOfStudent[m][s] = individual.getData().getStudentTakeSubject()[m][s] * slotStartOfSubject[s];
//                slotEndOfStudent[m][s] = individual.getData().getStudentTakeSubject()[m][s] * slotStartOfSubject[s] + individual.getData().getLengthOfSubject()[s] - 1;
            }

        }
        int[][] sortedStartSlotDesc = new int[individual.getData().getNumberOfStudents()][individual.getData().getNumberOfSubjects()];
//        int[][] sortedEndSlotDes = new int[individual.getData().getNumberOfStudents()][individual.getData().getNumberOfSubjects()];
        for (int m = 0; m < individual.getData().getNumberOfStudents(); m++) {
            sortedStartSlotDesc[m] = Arrays.copyOf(slotStartOfStudent[m], slotStartOfStudent[m].length);
            Arrays.sort(sortedStartSlotDesc[m]);
            reverseArray(sortedStartSlotDesc[m]);
        }
//        for (int m = 0; m < individual.getData().getNumberOfStudents(); m++) {
//            sortedEndSlotDes[m] = Arrays.copyOf(slotEndOfStudent[m], slotEndOfStudent[m].length);
//            Arrays.sort(sortedEndSlotDes[m]);
//            reverseArray(sortedEndSlotDes[m]);
//        }
        double payoffValueStudent = 0;
        for (int m = 0; m < individual.getData().getNumberOfStudents(); m++) {
            double payoffOneStudent = 0.0;
            if (individual.getData().getNumberOfSubjectsOfEachStudent()[m] > 1) {
                for (int i = 0; i < individual.getData().getNumberOfSubjectsOfEachStudent()[m] - 1; i++) {
                    double diff = sortedStartSlotDesc[m][i] - sortedStartSlotDesc[m][i + 1]
                            - (int) individual.getData().numberOfTotalSlots / individual.getData().getNumberOfSubjectsOfEachStudent()[m];
                    payoffOneStudent += Math.exp(Math.abs(diff));
                }
                payoffOneStudent = Math.log(payoffOneStudent) / (individual.getData().getNumberOfSubjectsOfEachStudent()[m] - 1);
            }
            payoffValueStudent += payoffOneStudent;
        }
        return payoffValueStudent / individual.getData().getNumberOfStudents();
    }

    private void reverseArray(int[] arr) {
        int start = 0;
        int end = arr.length - 1;
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
    }

    public double calPayoffInvigilator(int[][][] chromosome) {
        int[][] numberSlotScheduleInvigilator = new int[individual.getData().getNumberOfInvigilators()][individual.getData().getNumberOfExaminationDays()];
        for (int i = 0; i < chromosome[0][0].length; i++) {
            for (int d = 0; d < individual.getData().getNumberOfExaminationDays(); d++) {
                int count = 0;
                for (int t = individual.getData().getNumberOfSlotsPerDay() * d; t < individual.getData().getNumberOfSlotsPerDay() * (d + 1); t++) {
                    for (int s = 0; s < individual.getData().getNumberOfSubjects(); s++) {
                        count += chromosome[s][t][i];
                    }
                }
                if (count > 0) {
                    numberSlotScheduleInvigilator[i][d] = 1;
                }
            }
        }

        double payoffValueInvigilator;
        double payoff1 = 0;
        double payoff2 = 0;
        double w1 = 1.0 / 4;
        double w2 = 3.0 / 4;

        for (int i = 0; i < chromosome[0][0].length; i++) {
            for (int d = 0; d < individual.getData().getNumberOfExaminationDays(); d++) {
                payoff1 += (double) numberSlotScheduleInvigilator[i][d];
            }
        }

        for (int i = 0; i < individual.getData().getNumberOfInvigilators(); i++) {
            int totalSlotOfInvigilator = 0;
            for (int s = 0; s < individual.getData().getNumberOfSubjects(); s++) {
                for (int t = 0; t < individual.getData().getNumberOfTotalSlots(); t++) {
                    totalSlotOfInvigilator += chromosome[s][t][i];
                }
            }
            payoff2 += Math.abs(totalSlotOfInvigilator - individual.getData().getNumberOfSlotsRequiredForInvigilators()[i]);
        }
//        System.out.println(w1*payoff1/ individual.getData().getNumberOfInvigilators());
//        System.out.println(w2*payoff2/ individual.getData().getNumberOfInvigilators());
        payoffValueInvigilator = (w1 * payoff1 + w2 * payoff2) / individual.getData().getNumberOfInvigilators();
        return payoffValueInvigilator;
    }

    public double calPayoffP0(int[][][] chromosome) {
        double meanRoomEachSlot;
        int totalRooms = 0;
        for (int t = 0; t < chromosome[0].length; t++) {
            for (int s = 0; s < chromosome.length; s++) {
                for (int i = 0; i < chromosome[0][0].length; i++) {
                    totalRooms += chromosome[s][t][i];
                }
            }
        }
        meanRoomEachSlot = (double) totalRooms / individual.getData().getNumberOfTotalSlots();
        double payOffP0;
        double payOffP01 = 0;
//        double payOffP02 = 0;
//        double w01 = 19.0 / 20;
//        double w02 = 1.0 / 20;
        for (int t = 0; t < chromosome[0].length; t++) {
            double totalRoomsEachSlot = 0;
            for (int s = 0; s < chromosome.length; s++) {
                for (int i = 0; i < chromosome[0][0].length; i++) {
                    totalRoomsEachSlot += chromosome[s][t][i];
                }
            }
            payOffP01 += Math.pow(totalRoomsEachSlot - meanRoomEachSlot, 2);
        }
        payOffP01 = Math.sqrt(payOffP01 / (individual.getData().getNumberOfTotalSlots() - 1));
//        for (int t = 0; t < chromosome[0].length; t++) {
//            double totalRoomsEachSlot = 0;
//            for (int s = 0; s < chromosome.length; s++) {
//                for (int i = 0; i < chromosome[0][0].length; i++) {
//                    totalRoomsEachSlot += chromosome[s][t][i];
//                }
//            }
//            payOffP02 += Math.abs(totalRoomsEachSlot - individual.getData().getNumberOfRooms()) / (individual.getData().getNumberOfTotalSlots());
//        }
//        System.out.println(w01 * payOffP01);
//        System.out.println(w02 * payOffP02);
        payOffP0 = payOffP01;
        return payOffP0;
    }

    @Override
    public void run() {
        float fitness = calculateFitess();
        individual.setFitness(fitness);
    }

}
