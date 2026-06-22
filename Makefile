# FastQRGenerator — build, test and benchmark tasks. Requires only a JDK and Maven.

MVN := ./mvnw

# Benchmark options (override on the command line):
#   make benchmark VERSIONS=1,10,40 TARGET_MILLIS=2000 FORMAT=csv
VERSIONS      ?= 1,5,10,20,40
TARGET_MILLIS ?= 1500
FORMAT        ?= table

CLASSES   := target/classes
BENCH_OUT := target/benchmark

.PHONY: help compile test benchmark clean

help:
	@echo "Targets:"
	@echo "  make compile     - compile the library (skips tests)"
	@echo "  make test        - run the test suite"
	@echo "  make benchmark   - run the QR generation benchmark"
	@echo "  make clean       - remove build output"
	@echo ""
	@echo "Benchmark options:"
	@echo "  make benchmark VERSIONS=1,10,40 TARGET_MILLIS=2000"
	@echo "  make benchmark FORMAT=csv > results.csv"

compile:
	@$(MVN) -q -DskipTests compile

test:
	$(MVN) test

# Recipe lines are silenced (@) so stdout carries only the benchmark output,
# keeping `make benchmark FORMAT=csv > results.csv` clean. Progress goes to stderr.
benchmark: compile
	@mkdir -p $(BENCH_OUT)
	@javac -cp $(CLASSES) -d $(BENCH_OUT) benchmark/Benchmark.java
	@java -cp $(CLASSES):$(BENCH_OUT) Benchmark "$(VERSIONS)" "$(TARGET_MILLIS)" "$(FORMAT)"

clean:
	$(MVN) -q clean
