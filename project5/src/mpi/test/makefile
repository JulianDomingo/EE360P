EXECS=matrix_mult
MPICC?=mpicc

all : ${EXECS}
	
matrix_mult: matrix_mult.c 
	${MPICC} -o matrix_mult matrix_mult.c

clean:
	rm -f ${EXECS}
	
run:
	mpiexec -np 7 matrix_mult	
	mpiexec -np 5 matrix_mult
	mpiexec -np 4 matrix_mult
	mpiexec -np 3 matrix_mult
	mpiexec -np 2 matrix_mult
	mpiexec -np 1 matrix_mult
