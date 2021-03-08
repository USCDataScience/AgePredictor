# Author Age Prediction
This is a author age categorizer that leverages the [Apache OpenNLP](https://opennlp.apache.org/) Maximum Entropy Classifier. It takes a text sample and classifies it into the following age categories: xx-18|18-24|25-34|35-49|50-64|65-xx. 


# Pre-Requisites

  1. Download [Apache Spark 2.0.0](https://archive.apache.org/dist/spark/spark-2.0.0/spark-2.0.0-bin-hadoop2.7.tgz) and place in the local directory for this checkout.
  2. `export SPARK_HOME="spark-2.0.0-bin-hadoop2.7"`
  3. Run `bin/download-opennlp.sh` to download Apache OpenNLP models referenced below.
  4. Run `mvn clean install` to build the assembly jars. The key one you need is `age-predictor-assembly/target/age-predictor-assembly-1.1-SNAPSHOT-jar-with-dependencies.jar`. If you do not see this jar, investigate your Maven and Java issues. It should build fine with

```
openjdk version "13.0.2" 2020-01-14
OpenJDK Runtime Environment (build 13.0.2+8)
OpenJDK 64-Bit Server VM (build 13.0.2+8, mixed mode, sharing)
MT-202397:AgePredictor mattmann$ mvn --V
Apache Maven 3.6.1 (d66c9c0b3152b2e69ee9bac180bb8fcc8e6af555; 2019-04-04T12:00:29-07:00)
Maven home: /usr/local/Cellar/maven/3.6.1/libexec
Java version: 13.0.2, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/openjdk-13.0.2.jdk/Contents/Home
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.15.7", arch: "x86_64", family: "mac"     
```

# QuickStart

  1. Follow the instructions to perform training, and build yourself a `model/en-ageClassify.bin` file
     *  `bin/authorage AgeClassifyTrainer -model model/en-ageClassify.bin -lang en -data data/sample_train.txt -encoding UTF-8`
  2. Run the Age prediction with the sample data
     * `bin/authorage AgePredict ./model/classify-unigram.bin ./model/regression-global.bin  data/sample_test.txt < data/sample_test.txt`
  3. Run the Age prediction and grep out the predictions from the sample data
     * `bin/authorage AgePredict ./model/classify-unigram.bin ./model/regression-global.bin  data/sample_test.txt < data/sample_test.txt 2>&1 | grep "Prediction"`
     * If you see as output from the above command you're good!

```
Prediction: 33.25378998833527
Prediction: 31.67628280063772
```


    

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
