package danopkeefe.ksql.udf;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;
import io.confluent.ksql.function.udf.UdfParameter;

import java.util.*;

@UdfDescription(name = "sentiment", description = "Determine sentiment as an Integer [-2,2]")
public class SentimentUdf {

    @Udf(description = "Determine sentiment as an Integer [-2,2] from a String")
    public Integer sentiment(@UdfParameter("string") final String string) {
        return getAverageSentiment(string).getValue();
    }


    Sentiment getAverageSentiment(final String string) {
        if(null == string || string.trim().isEmpty()) {
            return Sentiment.NEUTRAL;
        } else {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            Annotation annotation = pipeline.process(string);
            List<Sentiment> sentenceSentiments = new ArrayList<>();

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                Sentiment sentiment = getSentimentFromPredictedClass(RNNCoreAnnotations.getPredictedClass(tree));
                sentenceSentiments.add(sentiment);
            }
            long meanSentiment = Math.round(sentenceSentiments.stream().mapToInt(Sentiment::getValue).average().orElse(0));
            return Sentiment.fromInt((int) meanSentiment);
        }
    }

    Sentiment getSentimentFromPredictedClass(int predictedClass) {
        switch (predictedClass) {
            case 0:
                return Sentiment.VERY_NEGATIVE;
            case 1:
                return Sentiment.NEGATIVE;
            case 3:
                return Sentiment.POSITIVE;
            case 4:
                return Sentiment.VERY_POSITIVE;
            default:
                return Sentiment.NEUTRAL;
        }
    }

    enum Sentiment {
        VERY_NEGATIVE(-2),
        NEGATIVE(-1),
        NEUTRAL(0),
        POSITIVE(1),
        VERY_POSITIVE(2);

        private final int value;

        Sentiment(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
        private static final Map<Integer, Sentiment> intToTypeMap = new HashMap<Integer, Sentiment>();
        static {
            for (Sentiment sentiment : Sentiment.values()) {
                intToTypeMap.put(sentiment.value, sentiment);
            }
        }

        public static Sentiment fromInt(int i) {
            return intToTypeMap.get(i);
        }
    }
}
