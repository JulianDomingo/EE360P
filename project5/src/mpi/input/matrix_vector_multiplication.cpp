/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

#include <iostream>
#include <fstream>
#include <vector>
#include <fstream>
#include <mpi.h>

#define ROOT_PROCESS 0

int main(int argc, char *argv[]) {
    int number_of_processors, processor_rank;
    
    // Initialize vector and matrix for MPI processes.
    std::fstream vector_file("vector-1.txt", std::ios_base::in);
    std::fstream matrix_file("matrix-1.txt", std::ios_base::in);

    int vector_number, matrix_number; 
    int counter = 0, row = 0;
    bool matrix_row_size_found = false;

    std::vector<int> input_vector;
    while (vector_file >> vector_number) {
        input_vector.push_back(vector_number);    
    } 

    std::vector< std::vector<int> > input_matrix;
    while (matrix_file >> matrix_number) {
        if (!matrix_row_size_found) { 
            matrix_row_size_found = true;
            input_matrix.resize(matrix_number);
            //std::cout << "Matrix row size: " + std::to_string(matrix_number) << std::endl;
        }        
        else {
            input_matrix[row].push_back(matrix_number); 
            counter++;
        
            if (counter == input_vector.size()) {
                row++; 
                counter = 0;
            }
        }
    }


    int **matrix;
    // Root process puts matrix in heap so it's accessible to everybody. 
    if (is_root_process(processor_rank)) {
        matrix = new int*[input_matrix.size()];
        for (int col = 0; col < input_matrix.size(); col++) {
            matrix[col] = new int[input_vector.size()];
        }
    } 

   
    // Initialize receiver buffer, containing the rows which the specified processor is allowed to touch. 
    int receiver_buffer[1000];

    // Start processes
    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &processor_rank);
    MPI_Comm_rank(MPI_COMM_WORLD, &number_of_processors);

    
    int remainder_chunk_rows = (input_matrix.size() * input_matrix.size()) % number_of_processors;
    int starting_index = 0;

    int *chunk_sizes = new int[sizeof(int) * number_of_processes];
    int *starting_index_of_chunks = new int[sizeof(int) * number_of_processes];



    // Find:
    // 1. "starting_index_of_chunks" - Starting indexes of row chunks to multiply with vector
    // 2. "chunk_sizes" - Size of row chunks to multiply with vector for every process
    for (int processor = 0; processor < number_of_processes; processor++) {
        chunk_sizes[processor] = (input_matrix.size() * input_matrix.size()) / number_of_processors;
        if (remainder_chunk_rows > 0) {
            chunk_sizes[processor]++;
            remainder_chunk_rows--;
        } 
        
        starting_index_of_chunks[processor] = starting_index;
        starting_index = chunk_sizes[processor];
    }

    // Broadcast designated row chunks to run row-wise vector multiplication for each process.
    MPI_Scatterv(&matrix, chunk_sizes, starting_index_of_chunks, MPI_INT, &receiver_buffer, 1000, MPI_INT, 0, MPI_COMM_WORLD); 

    // Compute row-wise vector multiplication for current process
    




    // Wait for all processes to be done with their respective matrix computations.
    MPI_Barrier(MPI_COMM_WORLD);

    // TODO: Store results in result.txt.  


    // Deallocate objects placed on heap.
    if (is_root_process(processor_rank)) {
        for (int row = 0; row < matrix.size(); row++) {
            delete [] matrix[row];
        }
        delete [] matrix;
    }
    delete [] chunk_sizes;
    delete [] starting_index_of_chunks;


    // Clean up MPI!
    MPI_Finalize();
}

bool is_root_process(int rank) {
    return rank == 0;
}
