#!/usr/bin/env bash
###########################################################
# Change the following values to preprocess a new dataset.
# TRAIN_DIR, VAL_DIR and TEST_DIR should be paths to      
#   directories containing sub-directories with .java files
#   each of {TRAIN_DIR, VAL_DIR and TEST_DIR} should have sub-dirs,
#   and data will be extracted from .java files found in those sub-dirs).
# DATASET_NAME is just a name for the currently extracted 
#   dataset.                                              
# MAX_CONTEXTS is the number of contexts to keep for each 
#   method (by default 200).                              
# WORD_VOCAB_SIZE, PATH_VOCAB_SIZE, TARGET_VOCAB_SIZE -   
#   - the number of words, paths and target words to keep 
#   in the vocabulary (the top occurring words and paths will be kept). 
#   The default values are reasonable for a Tesla K80 GPU 
#   and newer (12 GB of board memory).
# NUM_THREADS - the number of parallel threads to use. It is 
#   recommended to use a multi-core machine for the preprocessing 
#   step and set this value to the number of cores.
# PYTHON - python3 interpreter alias.
TRAIN_DIR=tmp
VAL_DIR=tmp
TEST_DIR=tmp
DATASET_DIR=data_path
DATASET_NAME=java_captioning
MAX_DATA_CONTEXTS=1000
MAX_CONTEXTS=200
SUBTOKEN_VOCAB_SIZE=186277
TARGET_VOCAB_SIZE=26347
NUM_THREADS=64
PYTHON=python
#PYTHON=python3
###########################################################

TRAIN_DATA_FILE=${DATASET_DIR}/captioning_training.txt
VAL_DATA_FILE=${DATASET_DIR}/captioning_validation.txt
TEST_DATA_FILE=${DATASET_DIR}/captioning_test.txt

mkdir -p data_c2s
mkdir -p data_c2s/${DATASET_NAME}

TARGET_HISTOGRAM_FILE=data_c2s/${DATASET_NAME}/${DATASET_NAME}.histo.tgt.c2s
SOURCE_SUBTOKEN_HISTOGRAM=data_c2s/${DATASET_NAME}/${DATASET_NAME}.histo.ori.c2s
NODE_HISTOGRAM_FILE=data_c2s/${DATASET_NAME}/${DATASET_NAME}.histo.node.c2s

echo "Creating histograms from the training data"
cat ${TRAIN_DATA_FILE} | cut -d' ' -f1 | tr '|' '\n' | awk '{n[$0]++} END {for (i in n) print i,n[i]}' > ${TARGET_HISTOGRAM_FILE}
cat ${TRAIN_DATA_FILE} | cut -d' ' -f2- | tr ' ' '\n' | cut -d',' -f1,3 | tr ',|' '\n' | awk '{n[$0]++} END {for (i in n) print i,n[i]}' > ${SOURCE_SUBTOKEN_HISTOGRAM}
cat ${TRAIN_DATA_FILE} | cut -d' ' -f2- | tr ' ' '\n' | cut -d',' -f2 | tr '|' '\n' | awk '{n[$0]++} END {for (i in n) print i,n[i]}' > ${NODE_HISTOGRAM_FILE}


# C:\Users\User\lim\code2vec-master2>python preprocess.py --train_data data_path/captioning_training.txt --test_data data_path/captioning_test.txt --val_data data_path/captioning_validation.txt --max_contexts 200 --word_vocab_size 1301136 --path_vocab_size 911417   --target_vocab_size 261245 --word_histogram data_c2v/java_small/java_small.histo.ori.c2v --path_histogram data_c2v/java_small/java_small.histo.path.c2v  --target_histogram data_c2v/java_small/java_small.histo.tgt.c2v  --output_name data_c2v/java_small/java_small
