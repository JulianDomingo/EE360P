EXECS=matrix_vector_multiplication
MPICPP?=mpic++

all : ${EXECS}

matrix_vector_multiplication: matrix_vector_multiplication.cpp
	${MPICPP} -o matrix_vector_multiplication matrix_vector_multiplication.cpp

clean:
	rm -f ${EXECS}
