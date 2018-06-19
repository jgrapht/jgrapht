package org.jgrapht.util;

import java.util.Arrays;
import java.util.List;

public class RadixSort {

    public static final int MAX_DIGITS = 32;
    public static final int MAX_D = 4;
    public static final int SIZE_RADIX = 1 << (MAX_DIGITS / MAX_D);
    public static final int MASK = SIZE_RADIX - 1;

    private static int[] C = new int[SIZE_RADIX];

    private RadixSort(){
    }

    private static void radixSort(int A[], int N, int B[], int C[]) {
        for (int d = 0, shift = 0; d < MAX_D; d++, shift += (MAX_DIGITS / MAX_D)) {
            Arrays.fill(C, 0);

            for (int i = 0; i < N; ++i)
                C[(A[i] >> shift) & MASK]++;

            for (int i = 1; i < SIZE_RADIX; ++i)
                C[i] += C[i - 1];

            for (int i = N - 1; i >= 0; i--)
                B[--C[(A[i] >> shift) & MASK]] = A[i];

            System.arraycopy(B, 0, A, 0, N);
        }
    }

    public static void sort(List<Integer> list){
        final int n = list.size();
        int[] A = new int[n];
        int[] B = new int[n];

        for (int i = 0; i < n; i++) {
            A[i] = list.get(i);
        }

        radixSort(A, n, B, C);

        for (int i = 0; i < n; i++) {
            list.set(i, A[i]);
        }
    }
}
