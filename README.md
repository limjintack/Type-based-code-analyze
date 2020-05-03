# Type-based-code-analyze

## 1. Introduction
We conducted a study to analyze the source code using a large-scale source code, and this repository consists of files used for the study. This study was conducted based on the [code2seq](https://github.com/tech-srl/code2seq) study. Our study differs from the previous one in that it is pre-processed by adding type information to the AST in the pre-processing process.

So, except for the Extractor, these are mostly similar files, and we would like to thank uri-alon for [code2seq](https://github.com/tech-srl/code2seq).
<br>
<br>
## 2. How to Run Type based code analyze model
### 2-1. Data set preparation
You can do two things with this model. The first task is a function name prediction task, and the second task is code captioning. Therefore, an appropriate data set for both tasks is required.
For function name prediction, three datasets (Small, Medium, Large) of [code2seq](https://github.com/tech-srl/code2seq) were used, and the dataset can be obtained from the repository.
In the case of code captioning, the [CONCODE](https://github.com/sriniiyer/concode) data set was used, and this can also be obtained from the corresponding repository.
### 2-2. Data preprocess
We provide an extractor for function name prediction and an extractor for code captioning. Therefore, according to the purpose of the work, select Extractor from [interactive_predict.py](https://github.com/limjintack/Type-based-code-anayze/blob/master/src/interactive_predict.py) and proceed with the preprocessing.

After selecting a preprocessor, preprocessing is performed through the following command.
```
bash preprocess.sh
```

<br>

**In Windows:** <br>
Since we were training in a Windows environment, we divided the work more flexibly and went through the preprocessing process. That is, preprocessing was performed by sequentially using the following commands.
```
python JavaExtractor/extract.py --dir Dataset\java-small/test --max_path_length 8 --max_path_width 2 --jar JavaExtractor/JavaExtractor.jar > tmp/small_test.txt
python JavaExtractor/extract.py --dir Dataset\java-small/train --max_path_length 8 --max_path_width 2 --jar JavaExtractor/JavaExtractor.jar > tmp/small_train.txt
python JavaExtractor/extract.py --dir Dataset\java-small/validation --max_path_length 8 --max_path_width 2 --jar JavaExtractor/JavaExtractor.jar > tmp/small_validation.txt

bash preprocess_make_c2s.sh

python preprocess.py --train_data tmp/small_training.txt --test_data tmp/small_test.txt --val_data tmp/small_validation.txt --max_contexts 200 --max_data_contexts 1000 --subtoken_vocab_size 186277  --target_vocab_size 26347 --subtoken_histogram data_c2s/java_small/java_small.histo.ori.c2s --node_histogram data_c2s/java_small/java_small.histo.node.c2s --target_histogram data_c2s/java_small/java_small.histo.tgt.c2s --output_name data_c2s/java_small/java_small
```
### 2-3. Model training
This study uses the model used in [code2seq](https://github.com/tech-srl/code2seq), and training can be done through the following command.
```
bash train.sh
```
<br>

**In Windows:** <br>
Similar to the preprocessing, the commands were released and used in the Windows environment. Therefore, you can train through the following command.
```
mkdir models
mkdir models/small
python code2seq.py --data data_c2s/java_small/java_small --test data_c2s/java_small/java_small.val.c2s --save models/small/saved_model
```
### 2-4. Prediction
When the model training is completed, the function name prediction can be performed using this. Write the code of the function to be predicted in the [Input.java](https://github.com/limjintack/Type-based-code-anayze/blob/master/src/Input.java) file in the corresponding directory and proceed with the prediction through the following command. The model uses the 20th trained model under the assumption that 20 epochs have been learned, so the model_iter20 file is loaded and used.
```
python code2seq.py --load models/java_small/model_iter20 --predict
```

<br>

## 3. Dependency
This study was conducted with the support of high-performance computing servers. The server allocates containers for each host using Docker, and the main specifications of the server and the main specifications of the containers allocated to us are as follows.

specifications of the server 
- GPU : Tesla V100 32GB
- HDD : 700GB
- CPU : Xeon Gold 6126(2.6HGz) 12코어
- RAM : 96GB

specifications of the container
- OS : Ubuntu 18.04.2 LTS
- Python version : Python 3.6.8
- Python package :
  - tensorflow-gpu 1.14.0
  - tensorflow-estimator 1.14.0
  - tensorboard 1.14.0
  - Keras-Applications 1.0.8
  - Keras-Preprocessing 1.1.0
  - matplotlib 3.1.1
  - numpy 1.17.2
  - rouge 0.3.2










