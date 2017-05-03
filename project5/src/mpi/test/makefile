EXECS=matrix_mult
CLEAN=matrix_mult result.txt
MPICC?=mpicc

all : ${EXECS}
	
matrix_mult: matrix_mult.c 
	${MPICC} -o matrix_mult matrix_mult.c

clean:
	rm -f ${CLEAN}
