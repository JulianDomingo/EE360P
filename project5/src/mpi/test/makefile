EXECS=multiplication_matrix_vector
MPICC?=mpicc

all : ${EXECS}
	
multiplication_matrix_vector: multiplication_matrix_vector.c
	${MPICC} -o multiplication_matrix_vector multiplication_matrix_vector.c

clean:
	rm -f ${EXECS}
	
run:
	mpiexec -np 6 multiplication_matrix_vector
	mpiexec -np 3 multiplication_matrix_vector
