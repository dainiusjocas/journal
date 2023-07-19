package lt.jocas.vespa.linguistics;

import com.yahoo.language.Language;
import com.yahoo.language.process.StemMode;
import com.yahoo.language.process.Token;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LuceneTokenizerTest {

    @Test
    public void testTokenizer() {
        String text = "This is my Text";
        var tokenizer = new LuceneTokenizer(new LuceneAnalysisConfig.Builder().build());
        Iterable<Token> tokens = tokenizer
                .tokenize(text, Language.ENGLISH, StemMode.ALL, true);
        Iterator<Token> tokenIterator = tokens.iterator();
        assertToken("this", tokenIterator);
        assertToken("is", tokenIterator);
        assertToken("my", tokenIterator);
        assertToken("text", tokenIterator);
    }

    private void assertToken(String tokenString, Iterator<Token> tokens) {
        Token t = tokens.next();
        assertEquals(tokenString, t.getTokenString());
    }

    private List<Token> iterableToList(Iterable<Token> tokens) {
        List<Token> tokenList = new ArrayList<>();
        tokens.forEach(tokenList::add);
        return tokenList;
    }

    private List<String> tokenStrings(Iterable<Token> tokens) {
        List<String> tokenList = new ArrayList<>();
        tokens.forEach(token -> {
            tokenList.add(token.getTokenString());
        });
        return tokenList;
    }

    @Test
    public void testAnalyzerConfiguration() {
        String languageCode = Language.ENGLISH.languageCode();
        LuceneAnalysisConfig enConfig = new LuceneAnalysisConfig.Builder().analysis(
                Map.of(languageCode,
                        new LuceneAnalysisConfig
                                .Analysis
                                .Builder()
                                .tokenFilters(List.of(
                                        new LuceneAnalysisConfig
                                                .Analysis
                                                .TokenFilters
                                                .Builder()
                                                .name("englishMinimalStem"),
                                        new LuceneAnalysisConfig
                                                .Analysis
                                                .TokenFilters
                                                .Builder()
                                                .name("uppercase"))))
        ).build();
        LuceneLinguistics linguistics = new LuceneLinguistics(enConfig);
        Iterable<Token> tokens = linguistics
                .getTokenizer()
                .tokenize("Dogs and cats", Language.ENGLISH, StemMode.ALL, false);
        assertEquals(List.of("DOG", "AND", "CAT"), tokenStrings(tokens));
    }

    @Test
    public void testEnglishStemmerAnalyzerConfiguration() {
        String languageCode = Language.ENGLISH.languageCode();
        LuceneAnalysisConfig enConfig = new LuceneAnalysisConfig.Builder().analysis(
                Map.of(languageCode,
                        new LuceneAnalysisConfig.Analysis.Builder().tokenFilters(List.of(
                                new LuceneAnalysisConfig
                                        .Analysis
                                        .TokenFilters
                                        .Builder()
                                        .name("englishMinimalStem"))))
        ).build();
        LuceneLinguistics linguistics = new LuceneLinguistics(enConfig);
        Iterable<Token> tokens = linguistics
                .getTokenizer()
                .tokenize("Dogs and Cats", Language.ENGLISH, StemMode.ALL, false);
        assertEquals(List.of("Dog", "and", "Cat"), tokenStrings(tokens));
    }

    @Test
    public void testStemmerWithStopWords() {
        String languageCode = Language.ENGLISH.languageCode();
        LuceneAnalysisConfig enConfig = new LuceneAnalysisConfig.Builder().analysis(
                Map.of(languageCode,
                        new LuceneAnalysisConfig.Analysis.Builder().tokenFilters(List.of(
                                new LuceneAnalysisConfig
                                        .Analysis
                                        .TokenFilters
                                        .Builder()
                                        .name("englishMinimalStem"),
                                new LuceneAnalysisConfig
                                        .Analysis
                                        .TokenFilters
                                        .Builder()
                                        .name("stop")
                                        .conf("words", "stopwords.txt"))))
        ).build();
        LuceneLinguistics linguistics = new LuceneLinguistics(enConfig);
        Iterable<Token> tokens = linguistics
                .getTokenizer()
                .tokenize("Dogs and Cats", Language.ENGLISH, StemMode.ALL, false);
        assertEquals(List.of("Dog", "Cat"), tokenStrings(tokens));
    }
}
