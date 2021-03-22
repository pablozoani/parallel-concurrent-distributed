package tools;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static tools.EvaluationTools.printlnOf;

public class MatrixTools {

    /**
     * Create a square matrix with the given side length.
     * 
     * @param sideLength The length of the width and the height.
     * @return A matrix with the numbers from 1 to (sideLength * sideLength).
     */
    public static double[][] createRangeSquareMatrix(final int sideLength) {
        return createRangeMatrix(sideLength, sideLength);
    }

    /**
     * Create a matrix with the given dimensions.
     * 
     * @param height The number of rows in the matrix.
     * @param width The number of columns in the matrix.
     * @return A matrix with the numbers from 1 to (width * height).
     */
    public static double[][] createRangeMatrix(
        final int height,
        final int width
    ) {
        final double[][] output = new double[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                output[i][j] = j + i * width + 1.0;
        return output;
    }

    /**
     * Compute the multiplication of two matrices.
     * 
     * @param A First factor.
     * @param B Second factor.
     * @return The product of the two matrices.
     */
    public static double[][] productOf(
        final double[][] A,
        final double[][] B
    ) {
        final double[][] C = resultMatrixOf(A, B);
        withMatrixIndexes((i, j) ->
            computeMatrixCell(A, B, i, j, C),
            C
        );
        return C;
    }

    /**
     * Call the consumer passing to it the indexes of a matrix
     * whose height.
     * 
     * @param consumer Code that receives all the indexes of
     *                 the cells in the matrix.
     * @param matrix   A matrix to provide the height and width.
     */
    public static void withMatrixIndexes(
        final BiConsumer<Integer, Integer> consumer,
        final double[][] matrix
    ) {
        for (int i = 0; i < heightOf(matrix); i++)
            for (int j = 0; j < widthOf(matrix); j++)
                consumer.accept(i, j);
    }

    /**
     * Create a result matrix padded with zeroes whose height equals
     * the length of A and width equals to the length of the first
     * element of B. If the length of B is less than 0, an
     * IndexOutOfBoundsException is thrown. The width of A must be
     * equal to the height of B.
     * 
     * @param A First factor
     * @param B Second factor
     * @return A matrix padded with zeroes
     */
    public static double[][] resultMatrixOf(double[][] A, double[][] B) {
        if (widthOf(A) != heightOf(B))
            throw new RuntimeException("widthOf(A) != heightOf(B)");
        return new double[heightOf(A)][widthOf(B)];
    }

    public static int numberOfElementsIn(double[][] matrix) {
        return heightOf(matrix) * widthOf(matrix);
    }

    /**
     * @return The height of the matrix
     */
    public static int heightOf(double[][] matrix) {
        return matrix.length;
    }

    /**
     * @return The width of the matrix
     */
    public static int widthOf(double[][] matrix) {
        return matrix[0].length;
    }

    public static void multiplyRangeOfCells(
        final double[][] A,
        final double[][] B,
        final int startElementInclusive,
        final int endElementExclusive,
        final double[][] C
    ) {
        final int[] location = new int[2];
        IntStream.range(startElementInclusive, endElementExclusive)
            .parallel()
            .forEach(i -> {
                cellLocationIn(i, C, location);
                computeMatrixCell(A, B, location[0], location[1], C);
            });
//        for (int i = startElementInclusive; i < endElementExclusive; i++) {
//            cellLocationIn(i, C, location);
//            computeMatrixCell(A, B, location[0], location[1], C);
//        }
    }

    /**
     * Compute the cell for C[i][j] from A and B.
     * 
     * @param A First factor.
     * @param B Second factor.
     * @param i Height index of the cell.
     * @param j Width index of the cell.
     * @param C The matrix that contains the cell.
     */
    public static void computeMatrixCell(
        final double[][] A,
        final double[][] B,
        final int i,
        final int j,
        final double[][] C
    ) {
        C[i][j] = 0;
        for (int k = 0; k < A[i].length; k++)
            C[i][j] += A[i][k] * B[k][j];
    }

    public static void cellLocationIn(
        final int cellIndex,
        final double[][] matrix,
        final int[] result
    ) {
        final int temp = widthOf(matrix);
        result[0] = cellIndex / temp;
        result[1] = cellIndex % temp;
    }

    public static void printMatrix(double[][] matrix) {
        for (double[] doubles : matrix) {
            for (double aDouble : doubles) System.out.printf(Locale.US, "%,11.2f ", aDouble);
            System.out.println();
        }
        System.out.println();
    }

    public static void printMatrix(double[][] matrix, String title) {
        printlnOf('-', title.length());
        System.out.println(title);
        printlnOf('-', title.length());
        printMatrix(matrix);
    }

    public static boolean matrixEquals(double[][] A, double[][] B) {
        if (A == B) return true;
        if (A == null ||
            B == null ||
            heightOf(A) != heightOf(B) ||
            widthOf(A) != widthOf(B)
        ) return false;
        for (int i = 0; i < heightOf(A); i++)
            for (int j = 0; j < widthOf(A); j++)
                if (A[i][j] != B[i][j])
                    return false;
        return true;
    }
}
