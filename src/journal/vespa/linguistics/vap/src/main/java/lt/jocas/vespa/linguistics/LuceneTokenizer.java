package lt.jocas.vespa.linguistics;

import com.yahoo.language.Language;
import com.yahoo.language.process.StemMode;
import com.yahoo.language.process.Token;
import com.yahoo.language.process.Tokenizer;
import com.yahoo.language.simple.SimpleToken;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneTokenizer implements Tokenizer {

    private final static String FIELD_NAME = "field";

    private final AnalyzerFactory analyzerFactory;

    public LuceneTokenizer(LuceneAnalysisConfig config) {
        this.analyzerFactory = new AnalyzerFactory(config);
    }

    @Override
    public Iterable<Token> tokenize(String input, Language language, StemMode stemMode, boolean removeAccents) {
        if (input.isEmpty()) return List.of();

        return textToTokens(input, analyzerFactory.getAnalyzer(language, stemMode, removeAccents));
    }

    private Iterable<Token> textToTokens(String input, Analyzer analyzer) {
        List<Token> tokens = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, input);

        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
            while(tokenStream.incrementToken()) {
                // TODO: add other attributes, length, position, etc.
                // TODO: what to do with cases when multiple tokens are inserted into the position?
                tokens.add(new SimpleToken(charTermAttribute.toString(), charTermAttribute.toString()));
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to analyze: " + input, e);
        }
        return tokens;
    }
}
