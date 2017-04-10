/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

#include <iostream>
#include <fstream>
#include <vector>
#include <fstream>

int main(int argc, char *argv[]) {
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

        

    // Print matrix
    for (int row = 0; row < input_matrix.size(); row++) {
        for (int col = 0; col < input_matrix[0].size(); col++) {
            std::cout << std::to_string(input_matrix[row].at(col)) + " ";
        }
        std::cout << std::endl;
    }
}
