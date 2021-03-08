# Author Age Prediction
This is a author age categorizer that leverages the [Apache OpenNLP](https://opennlp.apache.org/) Maximum Entropy Classifier. It takes a text sample and classifies it into the following age categories: xx-18|18-24|25-34|35-49|50-64|65-xx. 


# Pre-Requisites

  1. Download [Apache Spark 2.0.0](https://archive.apache.org/dist/spark/spark-2.0.0/spark-2.0.0-bin-hadoop2.7.tgz) and place in the local directory for this checkout.
  2. `export SPARK_HOME="spark-2.0.0-bin-hadoop2.7"`
  3. Run `bin/download-opennlp.sh` to download Apache OpenNLP models referenced below.



# QuickStart

  1. Follow the instructions to perform training, and build yourself a `model/en-ageClassify.bin` file
     *  `bin/authorage AgeClassifyTrainer -model model/en-ageClassify.bin -lang en -data data/sample_train.txt -encoding UTF-8`
  2. Run the Age prediction with the sample data
     * `bin/authorage AgePredict ./model/classify-unigram.bin ./model/regression-global.bin  data/sample_test.txt < data/sample_test.txt`
    

# Usage
### How to train an Age Classifier

Note: The training data should be a line-by-line, with each line starting with the age, or age category, followed by a tab and the text associated with the age. 

``` shell
Usage: bin/authorage AgeClassifyTrainer [-factory factoryName] [-featureGenerators featuregens] [-tokenizer tokenizer] -model modelFile [-params paramsFile] -lang language -data sampleData [-encoding charsetName]

Arguments description:
	-factory factoryName
        a sub-class of DoccatFactory where to get implementation and resources.
	-featureGenerators featuregens
	    comma separated feature generator classes. Bag of words default.
	-tokenizer tokenizer
        tokenizer implementation. WhitespaceTokenizer is used if not specified.
	-model modelFile
        output model file.
	-params paramsFile
	    training parameters file.
	-lang language
	    language which is being processed.
	-data sampleData
	    data to be used, usually a file name.
	-encoding charsetName
	    encoding for reading and writing text, if absent the system default is used.
```
Example Usage:
``` shell
bin/authorage AgeClassifyTrainer -model model/en-ageClassify.bin -lang en -data data/sample_train.txt -encoding UTF-8
```
Training data format - Age and text seperated by tab in each line like `<AGE><Tab><TEXT>`    
Sample training data-
```
12	I am just 12 year old
25	I am little bigger
35	I am mature
45	I am getting old
60	I am old like wine
```
### How to evaluate an Age Classifier Model

```shell
Usage: bin/authorage AgeClassifyEvaluator -model model [-misclassified true|false] -data sampleData [-encoding charsetName]

Arguments description:
	-model model
		the model file to be evaluated.
	-misclassified true|false
		if true will print false negatives and false positives.
	-data sampleData
		data to be used, usually a file name.
	-encoding charsetName
		encoding for reading and writing text, if absent the system default is used.
```

Example Usage:
```shell
bin/authorage AgeClassifyEvaluator -model model/en-ageClassify.bin -data data/sample_test.txt -encoding UTF-8
```

### How to run the Age Classifier

Note: Each document must be followed by an empty line to be detected as a separate case from the others.

```shell
Usage: bin/authorage AgeClassify model < documents
```

```shell
Usage: bin/authorage AgePredict ./model/classify-unigram.bin ./model/regression-global.bin  data/sample_test.txt < data/sample_test.txt
```

# Downloads
For AgePredict to work you need to download `en-pos-maxent.bin`, `en-sent.bin` and `en-token.bin` from [http://opennlp.sourceforge.net/models-1.5/](http://opennlp.sourceforge.net/models-1.5/) to `model/opennlp/`

# Citation:

If you use this work, please cite:

```
@article{hong2017ensemble,
  title={Ensemble Maximum Entropy Classification and Linear Regression for Author Age Prediction},
  author={Hong, Joey and Mattmann, Chris and Ramirez, Paul},
  booktitle={Information Reuse and Integration (IRI), 2017 IEEE 18th International Conference on},
  organization={IEEE}
  year={2017}
}
```

# Contributors
* Chris A. Mattmann, JPL & USC
* Joey Hong, Caltech
* Madhav Sharan, JPL & USC

# License 
[Apache License, version 2](http://www.apache.org/licenses/LICENSE-2.0)
