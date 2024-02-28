package myThread;

import java.util.Arrays;
import utils.Individual;

public class FitnessCalculateThread implements Runnable {

    private Individual individual;

    public FitnessCalculateThread(Individual individual) {
        this.individual = individual;
    }

    public float calculateFitess() {
        double w3 = 1 / 3.0;
        double w4 = 1 / 3.0;
        double w5 = 1 / 3.0;
        double fitnessValue;
        if (individual.passAllConstraints(individual.getChromosome())) {
            double payoffStudent = calPayoffStudent(individual.getChromosome());
            double payoffInvigilator = calPayoffInvigilator(individual.getChromosome());
            double payoffP0 = calPayoffP0(individual.getChromosome());
//            System.out.println("payoff student: " + payoffStudent);
//            System.out.println("pay off invigiglator: " + payoffInvigilator);
//            System.out.println("pay off pdt: " + payoffP0);
            fitnessValue = w3 * payoffStudent + w4 * payoffInvigilator + w5 * payoffP0;
        } else {
            fitnessValue = 10000;
        }
        return (float) fitnessValue;
    }

    public double calPayoffStudent(int[][][] chromosome) {
        int[][] subjectHeldAtSlot = individual.getSubjectHeldAtSlot();
        int[] slotStartOfSubject = individual.getSlotStartOfSubject();
        int[][] slotStartOfStudent = new int[individual.getData().getNumberOfStudents()][individual.getData().getNumberOfSubjects()];

        for (int m = 0; m < individual.getData().getNumberOfStudents(); m++) {
            for (int s = 0; s < individual.getData().getNumberOfSubjects(); s++) {
                slotStartOfStudent[m][s] = individual.getData().getStudentTakeSubject()[m][s] * slotStartOfSubject[s];
            }

        }
        int[][] sortedSlotDesc = new int[individual.getData().getNumberOfStudents()][individual.getData().getNumberOfSubjects()];
        for (int m = 0; m < individual.getData().getNumberOfStudents(); m++) {
            sortedSlotDesc[m] = Arrays.copyOf(slotStartOfStudent[m], slotStartOfStudent[m].length);
            Arrays.sort(sortedSlotDesc[m]);
            reverseArray(sortedSlotDesc[m]);
        }
        double payoffValueStudent = 0;
        for (int m = 0; m < individual.getData().getNumberOfStudents(); m++) {
            double payoffOneStudent = 0;
            for (int i = 0; i < individual.getData().getNumberOfSubjectsOfEachStudent()[m] - 1; i++) {
                payoffOneStudent += Math.abs(sortedSlotDesc[m][i] - sortedSlotDesc[m][i + 1]
                        - ((float) individual.getData().getNumberOfTotalSlots() / individual.getData().getNumberOfSubjectsOfEachStudent()[m]));
            }
            payoffValueStudent += payoffOneStudent / individual.getData().getNumberOfSubjectsOfEachStudent()[m];
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

        double payoffValueInvigilator = 0;
        double payoff1 = 0;
        double payoff2 = 0;
        double w1 = 1 / 2.0;
        double w2 = 1 / 2.0;

        for (int i = 0; i < chromosome[0][0].length; i++) {
            for (int d = 0; d < individual.getData().getNumberOfExaminationDays(); d++) {
                payoff1 += numberSlotScheduleInvigilator[i][d];
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
        payoffValueInvigilator = (w1 * payoff1 + w2 * payoff2) / individual.getData().getNumberOfInvigilators();
        return payoffValueInvigilator;
    }

    public double calPayoffP0(int[][][] chromosome) {
        double meanRoomEachSlot = 0;
        int totalRooms = 0;
        for (int t = 0; t < chromosome[0].length; t++) {
            for (int s = 0; s < chromosome.length; s++) {
                for (int i = 0; i < chromosome[0][0].length; i++) {
                    totalRooms += chromosome[s][t][i];
                }
            }
        }
        meanRoomEachSlot = (double) totalRooms / chromosome[0].length;
        double payOffP0 = 0;
        for (int t = 0; t < chromosome[0].length; t++) {
            int totalRoomsEachSlot = 0;
            for (int s = 0; s < chromosome.length; s++) {
                for (int i = 0; i < chromosome[0][0].length; i++) {
                    totalRoomsEachSlot += chromosome[s][t][i];
                }
            }
            payOffP0 += Math.pow(totalRoomsEachSlot - meanRoomEachSlot, 2);
        }
        payOffP0 = Math.sqrt(payOffP0 / ((individual.getData().getNumberOfTotalSlots() - 1) * individual.getData().getNumberOfTotalSlots()));
        return payOffP0;
    }

    @Override
    public void run() {
        float fitness = calculateFitess();
        individual.fitness = fitness;
    }
}
