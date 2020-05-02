# Type-based-code-anayze

## 1. Introduction
We conducted a study to analyze the source code using a large-scale source code, and this repository consists of files used for the study. This study was conducted based on the [code2seq](https://github.com/tech-srl/code2seq) study. Our study differs from the previous one in that it is pre-processed by adding type information to the AST in the pre-processing process.

So, except for the Extractor, these are mostly similar files, and we would like to thank uri-alon for [code2seq](https://github.com/tech-srl/code2seq).
<br>
<br>
## 2. How to Run Type based code anayze model
### 2-1. Data set preparation
You can do two things with this model. The first task is a function name prediction task, and the second task is code captioning. Therefore, an appropriate data set for both tasks is required.
For function name prediction, three datasets (Small, Medium, Large) of [code2seq](https://github.com/tech-srl/code2seq) were used, and the dataset can be obtained from the repository.
In the case of code captioning, the [CONCODE](https://github.com/sriniiyer/concode) data set was used, and this can also be obtained from the corresponding repository.
<br>
<br>
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
<br>

### 2-3. Model training
This study uses the model used in [code2seq](https://github.com/tech-srl/code2seq), and training using pre-processed data can be done through the following command.
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
<br>

### 2-4. Prediction




