package com.example.demo;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@SpringBootApplication
@RestController
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RequestMapping("/")
	public String defaultPage() {
		return "LLL";
	}

	@RequestMapping(value = "/train", method = RequestMethod.POST)
	public com.example.demo.Evaluation trainLinearClf(double lr, int batch_size, int epochs, int nLayers) throws Exception {
		int seed = 123;
		double learningRate = lr;
		int batchSize = batch_size;
		int nEpochs = epochs;

		int numInputs = 2;
		int numOutputs = 2;
		int numHiddenNodes = 20;

		final String filenameTrain  = new ClassPathResource("/classification/linear_data_train.csv").getFile().getPath();
		final String filenameTest  = new ClassPathResource("/classification/linear_data_eval.csv").getFile().getPath();

		//Load the training data:
		RecordReader rr = new CSVRecordReader();
//        rr.initialize(new FileSplit(new File("src/main/resources/classification/linear_data_train.csv")));
		rr.initialize(new FileSplit(new File(filenameTrain)));
		DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,2);

		//Load the test/evaluation data:
		RecordReader rrTest = new CSVRecordReader();
		rrTest.initialize(new FileSplit(new File(filenameTest)));
		DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,batchSize,0,2);

//		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
//				.seed(seed)
//				.iterations(1)
//				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//				.learningRate(learningRate)
//				.updater(Updater.NESTEROVS)     //To configure: .updater(new Nesterovs(0.9))
//				.list()
//				.layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
//						.weightInit(WeightInit.XAVIER)
//						.activation(Activation.RELU)
//						.build())
//				.layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//						.weightInit(WeightInit.XAVIER)
//						.activation(Activation.SOFTMAX).weightInit(WeightInit.XAVIER)
//						.nIn(numHiddenNodes).nOut(numOutputs).build())
//				.pretrain(false).backprop(true).build();

        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        NeuralNetConfiguration.ListBuilder listBuilder = builder.seed(seed).iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .updater(Updater.NESTEROVS)
                .list();

        listBuilder.layer(0, new DenseLayer.Builder().nIn(numInputs)
                .nOut(numHiddenNodes)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .build());

        for (int i = 1; i < 3; i++) {
            listBuilder = listBuilder.layer(i, new DenseLayer.Builder()
                    .weightInit(WeightInit.XAVIER)
                    .activation(Activation.RELU)
                    .nIn(numHiddenNodes).nOut(numHiddenNodes).build());
        }

        listBuilder = listBuilder.layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.SOFTMAX)
                .nIn(numHiddenNodes).nOut(numOutputs).build());

        MultiLayerConfiguration conf = listBuilder.pretrain(false).backprop(true).build();
		String json = conf.toJson();


		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new PostScoreIterListener(10));  //Print score every 10 parameter updates


		for ( int n = 0; n < nEpochs; n++) {
			model.fit( trainIter );
		}

		Evaluation eval = new Evaluation(numOutputs);
		while(testIter.hasNext()){
			DataSet t = testIter.next();
			INDArray features = t.getFeatureMatrix();
			INDArray lables = t.getLabels();
			INDArray predicted = model.output(features,false);

			eval.eval(lables, predicted);

		}

        com.example.demo.Evaluation evaluation = new com.example.demo.Evaluation();
		evaluation.setAccuracy(eval.accuracy());
		evaluation.setF1(eval.f1());
		evaluation.setPrecision(eval.precision());
		evaluation.setRecall(eval.recall());

		return evaluation;
	}
}
