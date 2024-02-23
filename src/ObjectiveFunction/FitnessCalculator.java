package ObjectiveFunction;

import Data.GeneticData;
import java.util.Arrays;

public class FitnessCalculator {

    private GeneticData data;

    public FitnessCalculator(GeneticData data) {
        this.data = data;
    }

    public float calculateFitess(int[][][] chromosome) {
        double w3 = 1 / 3.0;
        double w4 = 1 / 3.0;
        double w5 = 1 / 3.0;
        double fitnessValue;
        double payoffStudent = calPayoffStudent(chromosome);
        double payoffInvigilator = calPayoffInvigilator(chromosome);
        double payoffP0 = calPayoffP0(chromosome);
//            System.out.println("payoff student: " + payoffStudent);
//            System.out.println("pay off invigiglator: " + payoffInvigilator);
//            System.out.println("pay off pdt: " + payoffP0);
        fitnessValue = w3 * payoffStudent + w4 * 250 * payoffInvigilator + w5 * 5000 * payoffP0;
        return (float) fitnessValue;
    }

    public double calPayoffStudent(int[][][] chromosome) {
        int[][] subjectHeldAtSlot = createSubjectHeldAtSlot(chromosome);
        int[][] slotStartOfSubject = createSlotStartOfSubject(subjectHeldAtSlot);
        int[][] slotStartOfStudent = new int[data.getNumberOfStudents()][data.getNumberOfSubjects()];

        for (int m = 0; m < data.getNumberOfStudents(); m++) {
            for (int s = 0; s < data.getNumberOfSubjects(); s++) {
                slotStartOfStudent[m][s] = data.getStudentTakeSubject()[m][s] * slotStartOfSubject[s][0];
            }

        }
        int[][] sortedSlotDesc = new int[data.getNumberOfStudents()][data.getNumberOfSubjects()];
        for (int m = 0; m < data.getNumberOfStudents(); m++) {
            sortedSlotDesc[m] = Arrays.copyOf(slotStartOfStudent[m], slotStartOfStudent[m].length);
            Arrays.sort(sortedSlotDesc[m]);
            reverseArray(sortedSlotDesc[m]);
        }
        double payoffValueStudent = 0;
        for (int m = 0; m < data.getNumberOfStudents(); m++) {
            double payoffOneStudent = 0;
            for (int i = 0; i < data.getNumberOfSubjectsOfEachStudent()[m] - 1; i++) {
                payoffOneStudent += Math.abs(sortedSlotDesc[m][i] - sortedSlotDesc[m][i + 1]
                        - ((float) data.getNumberOfTotalSlots() / data.getNumberOfSubjectsOfEachStudent()[m]));
            }
            payoffValueStudent += payoffOneStudent;
        }
        return payoffValueStudent;
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
        int[][] numberSlotScheduleInvigilator = new int[chromosome[0][0].length][data.getNumberOfExaminationDays()];
        for (int i = 0; i < chromosome[0][0].length; i++) {
            for (int d = 0; d < data.getNumberOfExaminationDays(); d++) {
                int count = 0;
                for (int t = data.getNumberOfSlotsPerDay() * d; t < data.getNumberOfSlotsPerDay() * (d + 1); t++) {
                    for (int s = 0; s < data.getNumberOfSubjects(); s++) {
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
        double w1 = 1 / 4.0;
        double w2 = 3 / 4.0;

        for (int i = 0; i < chromosome[0][0].length; i++) {
            for (int d = 0; d < data.getNumberOfExaminationDays(); d++) {
                payoff1 += numberSlotScheduleInvigilator[i][d];
            }
        }

        for (int i = 0; i < data.getNumberOfInvigilators(); i++) {
            int totalSlotOfInvigilator = 0;
            for (int s = 0; s < data.getNumberOfSubjects(); s++) {
                for (int t = 0; t < data.getNumberOfTotalSlots(); t++) {
                    totalSlotOfInvigilator += chromosome[s][t][i];
                }
            }
            payoff2 += Math.abs(totalSlotOfInvigilator - data.getNumberOfSlotsRequiredForInvigilators()[i]);
        }
        payoffValueInvigilator = w1 * payoff1 + w2 * payoff2;
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
            for (int s = 0; s <  chromosome.length; s++) {
                for (int i = 0; i < chromosome[0][0].length; i++) {
                    totalRoomsEachSlot += chromosome[s][t][i];
                }
            }
            payOffP0 += Math.pow(totalRoomsEachSlot - meanRoomEachSlot, 2);
        }
        payOffP0 = Math.sqrt(payOffP0 / (data.getNumberOfTotalSlots() - 1));
        return payOffP0;
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
}
