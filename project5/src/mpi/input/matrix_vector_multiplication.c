#include <mpi.h>
#include <stdio.h>

int main(int argc, char** argv) {
    FILE *matrix_file;
    FILE *vector_file;

    matrix_file = fopen("matrix.txt", "r");
    vector_file = fopen("vector.txt", "r"); 

    int[][] matrix = 
    if (matrix_file && vector_file) {
    }
     
    
     
    MPI_INIT(NULL, NULL);
    int number_of_processors;
    MPI_Comm_size(MPI_COMM_WORLD, &number_of_processors);

    int processor_ranks;
    MPI_Comm_rank(MPI_COMM_WORLD, &processor_ranks);

    char processor_name[MPI_MAX_PROCESSOR_NAME];
    int processor_name_length;
    MPI_GET_processor_name(processor_name, &processor_name_length);
}


