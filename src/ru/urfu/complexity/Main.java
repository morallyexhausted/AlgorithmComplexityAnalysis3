package ru.urfu.complexity;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

/**
 * Практическая работа №3: Оценка сложности алгоритмов (Часть II)
 * Экспериментальная проверка сложности на работающей JVM.
 */
public class Main {

    private static final int[] SIZES = {
            10, 100, 1_000, 2_000, 4_000, 8_000, 16_000, 32_000, 64_000, 128_000
    };
    private static final int WARMUP_ITERATIONS = 5_000;
    private static final int MEASURE_RUNS = 5;
    private static final int BATCH_REPEATS = 1_000; // Для стабилизации замеров быстрых операций (O(1), O(log N))
    private static volatile long blackhole; // Защита от Dead Code Elimination (JIT)

    static void main(String[] args) {
        System.out.println("=== Запуск бенчмарка Практической работы №3 ===");
        Random random = new Random(42);

        try (PrintWriter csv = new PrintWriter(new FileWriter("benchmark_results.csv"))) {
            csv.println("Task;Case;N;Run1_ns;Run2_ns;Run3_ns;Run4_ns;Run5_ns;Avg_ns");

            runTask1Constant(csv, random);
            runTask2BinarySearch(csv, random);
            runTask3LinearSearch(csv, random);
            runTask4CountGreaterThan(csv, random);
            runTask5FindMax(csv, random);

            System.out.println("\nВсе замеры успешно завершены и сохранены в benchmark_results.csv");
        } catch (IOException e) {
            System.err.println("Ошибка записи результатов: " + e.getMessage());
        }
    }

    // =========================================================================
    // ЗАДАНИЕ 1. Константная сложность — O(1): доступ по индексу array[index]
    // =========================================================================
    private static void runTask1Constant(PrintWriter csv, Random random) {
        System.out.println("\n--- Задание 1: O(1) Доступ по индексу ---");
        for (int n : SIZES) {
            int[] array = generateSortedArray(n);
            int[] indices = new int[BATCH_REPEATS];
            for (int i = 0; i < BATCH_REPEATS; i++) {
                indices[i] = random.nextInt(n);
            }

            // Прогрев JVM
            for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                blackhole = array[indices[w % BATCH_REPEATS]];
            }

            double[] runs = new double[MEASURE_RUNS];
            for (int r = 0; r < MEASURE_RUNS; r++) {
                long start = System.nanoTime();
                long sum = 0;
                for (int i = 0; i < BATCH_REPEATS; i++) {
                    sum += array[indices[i]];
                }
                long end = System.nanoTime();
                blackhole = sum;
                runs[r] = (double) (end - start) / BATCH_REPEATS;
            }
            recordResult(csv, "Task1_O(1)_ArrayAccess", "RandomIndex", n, runs);
        }
    }

    // =========================================================================
    // ЗАДАНИЕ 2. Логарифмическая сложность — O(log N): бинарный поиск
    // =========================================================================
    public static int binarySearch(int[] array, int target) {
        int left = 0;
        int right = array.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (array[mid] == target) {
                return mid;
            } else if (array[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    private static void runTask2BinarySearch(PrintWriter csv, Random random) {
        System.out.println("\n--- Задание 2: O(log N) Бинарный поиск ---");
        String[] cases = {"FirstElement", "RandomElement", "AbsentElement"};

        for (String caseName : cases) {
            for (int n : SIZES) {
                int[] array = generateSortedArray(n);
                int target = selectTarget(array, caseName, random);

                // Прогрев JVM
                for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                    blackhole = binarySearch(array, target);
                }

                double[] runs = new double[MEASURE_RUNS];
                for (int r = 0; r < MEASURE_RUNS; r++) {
                    long start = System.nanoTime();
                    int res = 0;
                    for (int i = 0; i < BATCH_REPEATS; i++) {
                        res += binarySearch(array, target);
                    }
                    long end = System.nanoTime();
                    blackhole = res;
                    runs[r] = (double) (end - start) / BATCH_REPEATS;
                }
                recordResult(csv, "Task2_O(logN)_BinarySearch", caseName, n, runs);
            }
        }
    }

    // =========================================================================
    // ЗАДАНИЕ 3. Линейная сложность — O(N): линейный поиск
    // =========================================================================
    public static int linearSearch(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    private static void runTask3LinearSearch(PrintWriter csv, Random random) {
        System.out.println("\n--- Задание 3: O(N) Линейный поиск ---");
        String[] cases = {"FirstElement", "RandomElement", "AbsentElement"};

        for (String caseName : cases) {
            for (int n : SIZES) {
                int[] array = generateSortedArray(n);
                int target = selectTarget(array, caseName, random);

                // Прогрев JVM
                for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                    blackhole = linearSearch(array, target);
                }

                double[] runs = new double[MEASURE_RUNS];
                for (int r = 0; r < MEASURE_RUNS; r++) {
                    long start = System.nanoTime();
                    blackhole = linearSearch(array, target);
                    long end = System.nanoTime();
                    runs[r] = (double) (end - start);
                }
                recordResult(csv, "Task3_O(N)_LinearSearch", caseName, n, runs);
            }
        }
    }

    // =========================================================================
    // ЗАДАНИЕ 4. Линейная сложность — O(N): подсчет элементов > target
    // =========================================================================
    public static int countGreaterThan(int[] array, int target) {
        int count = 0;
        for (int value : array) {
            if (value > target) {
                count++;
            }
        }
        return count;
    }

    private static void runTask4CountGreaterThan(PrintWriter csv, Random random) {
        System.out.println("\n--- Задание 4: O(N) Подсчет элементов по условию ---");
        String[] cases = {"AllMatch", "RandomMatch", "NoneMatch"};

        for (String caseName : cases) {
            for (int n : SIZES) {
                int[] array = generateRandomArray(n, random);
                int target = switch (caseName) {
                    case "AllMatch" -> -1;                 // Все элементы >= 0 > -1
                    case "RandomMatch" -> n / 2;           // Примерно половина элементов > n/2
                    case "NoneMatch" -> n + 100;           // Ни один элемент < n не больше n+100
                    default -> 0;
                };

                // Прогрев JVM
                for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                    blackhole = countGreaterThan(array, target);
                }

                double[] runs = new double[MEASURE_RUNS];
                for (int r = 0; r < MEASURE_RUNS; r++) {
                    long start = System.nanoTime();
                    blackhole = countGreaterThan(array, target);
                    long end = System.nanoTime();
                    runs[r] = (double) (end - start);
                }
                recordResult(csv, "Task4_O(N)_CountCondition", caseName, n, runs);
            }
        }
    }

    // =========================================================================
    // ЗАДАНИЕ 5 (Самостоятельная работа). Линейная сложность — O(N):
    // Поиск максимального элемента в массиве (анализ работы branch predictor)
    // =========================================================================
    public static int findMax(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private static void runTask5FindMax(PrintWriter csv, Random random) {
        System.out.println("\n--- Задание 5: O(N) Поиск максимума (Самостоятельная работа) ---");
        String[] cases = {"Best_MaxAtStart", "Average_RandomOrder", "Worst_SortedAscending"};

        for (String caseName : cases) {
            for (int n : SIZES) {
                int[] array = switch (caseName) {
                    case "Best_MaxAtStart" -> generateDescendingArray(n); // Максимум в начале, ветка if не срабатывает
                    case "Average_RandomOrder" -> generateRandomArray(n, random); // Случайные обновления max
                    case "Worst_SortedAscending" -> generateSortedArray(n); // Максимум в конце, обновление max на каждом шаге
                    default -> generateRandomArray(n, random);
                };

                // Прогрев JVM
                for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                    blackhole = findMax(array);
                }

                double[] runs = new double[MEASURE_RUNS];
                for (int r = 0; r < MEASURE_RUNS; r++) {
                    long start = System.nanoTime();
                    blackhole = findMax(array);
                    long end = System.nanoTime();
                    runs[r] = (double) (end - start);
                }
                recordResult(csv, "Task5_O(N)_FindMax", caseName, n, runs);
            }
        }
    }

    // =========================================================================
    // Вспомогательные методы генерации данных и логирования
    // =========================================================================
    private static int[] generateSortedArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i * 2;
        }
        return arr;
    }

    private static int[] generateDescendingArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = (n - i) * 2;
        }
        return arr;
    }

    private static int[] generateRandomArray(int n, Random random) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = random.nextInt(n);
        }
        return arr;
    }

    private static int selectTarget(int[] sortedArray, String caseName, Random random) {
        return switch (caseName) {
            case "FirstElement" -> sortedArray[0];
            case "RandomElement" -> sortedArray[sortedArray.length / 2];
            case "AbsentElement" -> -1;
            default -> sortedArray[0];
        };
    }

    private static void recordResult(PrintWriter csv, String task, String caseName, int n, double[] runs) {
        double avg = Arrays.stream(runs).average().orElse(0.0);
        csv.printf("%s;%s;%d;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f%n",
                task, caseName, n, runs[0], runs[1], runs[2], runs[3], runs[4], avg);
        System.out.printf("%-28s | %-21s | N=%-7d | Avg = %8.2f ns%n", task, caseName, n, avg);
    }
}
