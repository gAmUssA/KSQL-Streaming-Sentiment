package danopkeefe.ksql.udf;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SentimentUdfTest {

    private static final String VERY_NEGATIVE_SENTENCE = "This is a disgusting party!";
    private static final String NEGATIVE_SENTENCE = "This party is gross.";
    private static final String NEUTRAL_SENTENCE = "This is a party.";
    private static final String POSITIVE_SENTENCE = "This party is good.";
    private static final String VERY_POSITIVE_SENTENCE = "This party is amazing!";

    @Test
    void sentiment() {
        SentimentUdf sentiment = new SentimentUdf();
        assertEquals(SentimentUdf.Sentiment.VERY_NEGATIVE,sentiment.getAverageSentiment(VERY_NEGATIVE_SENTENCE));
        assertEquals(SentimentUdf.Sentiment.NEGATIVE,sentiment.getAverageSentiment(NEGATIVE_SENTENCE));
        assertEquals(SentimentUdf.Sentiment.NEUTRAL,sentiment.getAverageSentiment(NEUTRAL_SENTENCE));
        assertEquals(SentimentUdf.Sentiment.POSITIVE,sentiment.getAverageSentiment(POSITIVE_SENTENCE));
        assertEquals(SentimentUdf.Sentiment.VERY_POSITIVE,sentiment.getAverageSentiment(VERY_POSITIVE_SENTENCE));
    }

    @Test
    void compoundSentiment() {
        String neutral =  String.join("  ", VERY_NEGATIVE_SENTENCE,VERY_POSITIVE_SENTENCE);
        List<String> neutralSentences = Arrays.asList(
                String.join("  ", VERY_NEGATIVE_SENTENCE,VERY_POSITIVE_SENTENCE),
                String.join("  ", NEGATIVE_SENTENCE,POSITIVE_SENTENCE),
                String.join("  ", NEUTRAL_SENTENCE,NEUTRAL_SENTENCE)
        );
        SentimentUdf sentiment = new SentimentUdf();
        neutralSentences.forEach(sentence ->
                assertEquals(SentimentUdf.Sentiment.NEUTRAL, sentiment.getAverageSentiment(sentence)));
    }

}