package org.jgrapht.alg;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Christophe Thiebaud
 * 
 * cf. https://en.wikipedia.org/wiki/Transitive_reduction
 * 
 * port from python example by Michael Clerx, posted as an answer to http://stackoverflow.com/questions/1690953/transitive-reduction-algorithm-pseudocode
 * 
 */
public class TransitiveReductionTest {

    @Test
    public void test() {

        int[][] matrix = new int[][] {
            {0, 1, 1, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1},
            {0, 0, 0, 0, 1},
            {0, 1, 0, 0, 0}
        };
        
        int[][] expected_path_matrix = new int[][] {
            {0, 1, 1, 1, 1},
            {0, 0, 0, 0, 0},
            {0, 1, 0, 1, 1},
            {0, 1, 0, 0, 1},
            {0, 1, 0, 0, 0}
        };
        
        int[][] expected_transitively_reduced_matrix = new int[][] {
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1},
            {0, 1, 0, 0, 0}
        };
        
        // System.out.println(Arrays.deepToString(matrix) + " original matrix");

        int n = matrix.length;

        // calc path matrix
        int[][] path_matrix = new int[n][n];
        {
            System.arraycopy(matrix, 0, path_matrix, 0, matrix.length);
            
            TransitiveReduction.transformToPathMatrix(path_matrix);
            // System.out.println(Arrays.deepToString(path_matrix) + " path matrix");
            Assert.assertArrayEquals(expected_path_matrix, path_matrix);
        }

        // calc transitive reduction
        {
            int[][] transitively_reduced_matrix = new int[n][n];
            System.arraycopy(path_matrix, 0, transitively_reduced_matrix, 0, matrix.length);
            
            TransitiveReduction.transitiveReduction(transitively_reduced_matrix);
            // System.out.println(Arrays.deepToString(transitively_reduced_matrix) + " transitive reduction");
            Assert.assertArrayEquals(expected_transitively_reduced_matrix, transitively_reduced_matrix);
        }
    }

}
