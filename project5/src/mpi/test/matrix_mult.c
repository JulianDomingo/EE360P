/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

#include "mpi.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

int process_rank;
int number_of_processes;

bool is_root_process(int rank) {
    return rank == 0;
}

int main(int argc, char **argv) {
    MPI_Init(NULL, NULL);
    MPI_Comm_rank(MPI_COMM_WORLD, &process_rank);
    MPI_Comm_size(MPI_COMM_WORLD, &number_of_processes);

    MPI_Datatype sending_type;

    int matrix_row_size, matrix_column_size;

    FILE *matrix_file;
    FILE *vector_file;

    int *matrix = NULL;
    int *vector = NULL;
    int *temporary_vector = NULL; 
    int *resultant_vector = NULL;

    int *chunk_sizes = (int *) malloc(sizeof(int) * number_of_processes);
    int *starting_index_of_chunks = (int *) malloc(sizeof(int) * number_of_processes);

    int *received_chunk_sizes = (int *) malloc(sizeof(int) * number_of_processes);
    int *received_starting_index_of_chunks = (int *) malloc(sizeof(int) * number_of_processes);


    // Determine matrix row and column size for all processes.

    // *** To make things simpler in getting MPI_Scatterv/MPI_Gatherv
    // to properly function, the matrix is passed as a 1D array stream
    // of numbers instead of a 2D array. This is due to most of the 
    // example MPI programs passing a 1D structure of data types as the
    // sending buffer.
    if (is_root_process(process_rank)) {
        if ((matrix_file = fopen("matrix.txt", "r")) == NULL) {
          printf("File does not exist.");
          return 0;
        }

        fscanf(matrix_file, "%d", &matrix_row_size);

        matrix_column_size = 0;
        temporary_vector = (int *) malloc(sizeof(int) * 1000);

        if ((vector_file = fopen("vector.txt", "r")) == NULL) {
          printf("File does not exist.");
          return 0;
        }
        while (fscanf(vector_file, "%d", &temporary_vector[matrix_column_size]) == 1) {
          matrix_column_size++;
        }

        fclose(vector_file);
    }


    // Broadcast matrix row and column sizes to all processes.
    MPI_Bcast(&matrix_row_size, 1, MPI_INT, 0, MPI_COMM_WORLD);
    MPI_Bcast(&matrix_column_size, 1, MPI_INT, 0, MPI_COMM_WORLD);

    if (process_rank < matrix_row_size) {
        int remainder = matrix_row_size % number_of_processes;
        int starting_index = 0;

        // Determines number of rows to run the matrix multiplication for each process.
        for (int process = 0; process < number_of_processes; process++) {
            chunk_sizes[process] = (matrix_row_size / number_of_processes) * matrix_column_size;
            
            if (remainder > 0) {
                chunk_sizes[process] += matrix_column_size;
                remainder--;
            }

            starting_index_of_chunks[process] = starting_index;
            starting_index += chunk_sizes[process];
            received_chunk_sizes[process] = chunk_sizes[process] / matrix_column_size;
            received_starting_index_of_chunks[process] = starting_index_of_chunks[process] / matrix_column_size;
        }

        vector = (int *) malloc(sizeof(int) * matrix_column_size);


        // Obtain vector and matrix data if root process.
        if (is_root_process(process_rank)) {
            for (int vector_index = 0; vector_index < matrix_column_size; vector_index++) {
                vector[vector_index] = temporary_vector[vector_index];
            }

            free(temporary_vector);
            
            matrix = (int *) malloc(sizeof(int) * matrix_row_size * matrix_column_size);
            
            for (int i = 0; i < matrix_row_size * matrix_column_size; i++) {
                fscanf(matrix_file, "%d", &matrix[i]);
            }
            
            fclose(matrix_file);
            resultant_vector = (int *) malloc(sizeof(int) * matrix_column_size);
        }

        int receiving_buffer[matrix_column_size * matrix_row_size];

        MPI_Scatterv(matrix, chunk_sizes, starting_index_of_chunks, MPI_INT, receiving_buffer, matrix_column_size * matrix_row_size,
                     MPI_INT, 0, MPI_COMM_WORLD);

        MPI_Bcast(vector, matrix_column_size, MPI_INT, 0, MPI_COMM_WORLD);


        // Compute partition of the row-wise matrix multiplication for each process.
        int matrix_computation[matrix_column_size][number_of_processes];
        int resultant_vector_value = 0;
        int rows_processed = 0;
        int column_index = 0;
        int matrix_index = 0;

        while (rows_processed != received_chunk_sizes[process_rank]) {
            resultant_vector_value += receiving_buffer[matrix_index] * vector[column_index];

            // Current row finished computation.
            if (column_index == matrix_column_size - 1) {
                matrix_computation[rows_processed][process_rank] = resultant_vector_value;
                resultant_vector_value = 0;
                rows_processed++;
                column_index = 0;
            } 
            else {
                column_index++;
            }

            matrix_index++;
        } 


        // Allocate MPI vector for passing data to process 0 and gather results.
        if (is_root_process(process_rank)) {
            // MPI_Type_vector: creates an MPI-based vector to store matrix computation result in MPI_Gatherv(). 
            MPI_Type_vector(received_chunk_sizes[process_rank], 1, number_of_processes, MPI_INT, &sending_type);
            MPI_Type_commit(&sending_type);

            int *send_buffer = &matrix_computation[0][process_rank];

            MPI_Gatherv(send_buffer, 1, sending_type, resultant_vector, received_chunk_sizes, received_starting_index_of_chunks, MPI_INT, 0, MPI_COMM_WORLD);
        }


        // Generate result vector if root process
        if (is_root_process(process_rank)) {
            FILE *result_file = fopen("result.txt", "w+");
             
            for (int element = 0; element < matrix_row_size; element++) {
              printf("%d ", resultant_vector[element]);
              fprintf(result_file, "%d ", resultant_vector[element]);
            }
            fclose(result_file);
        }
    }

    // Wait for all processes to finish their respective computations before ending MPI session.
    MPI_Barrier(MPI_COMM_WORLD);
    MPI_Finalize();
    return 0;
}
