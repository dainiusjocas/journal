package lt.jocas.vespa.linguistics;

import com.yahoo.language.Language;
import com.yahoo.language.process.StemMode;
import com.yahoo.language.process.Token;
import com.yahoo.language.process.Tokenizer;
import com.yahoo.language.simple.SimpleToken;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuceneTokenizer implements Tokenizer {

    // Here list of current analyzers per language
    // the idea is not to create analyzers until they are needed
    // Analyzers are thread safe so no need to recreate them for every document
    private final LuceneAnalysisConfig config;

    private final Map<Language, Analyzer> languageAnalyzers = new HashMap<>();

    public LuceneTokenizer(LuceneAnalysisConfig config) {
        this.config = config;
    }

    private Analyzer getAnalyzer(Language language) {
        if (null == languageAnalyzers.get(language)) {
            Analyzer analyzer = null;
            try {
                analyzer = CustomAnalyzer.builder()
                        .withTokenizer(
                                "standard", new HashMap<>(Map.of("maxTokenLength", "4")))
                        .build();
            } catch (IOException e) {
                // TODO: what to Use as an analyzer in case resources are missing?
                // Definitely log WARNING
                // Since the resources should be in VAP, unit tests must catch the problem and prevent
                // VAP being deployed
                throw new RuntimeException(e);
            }
            // Store for future uses
            languageAnalyzers.put(language, analyzer);
            return analyzer;
        } else {
            return languageAnalyzers.get(language);
        }
    }

    private Iterable<Token> textToTokens(String input, Analyzer analyzer) {
        List<Token> tokens = new ArrayList<>();
        TokenStream fieldName = analyzer.tokenStream("fieldName", input);

        CharTermAttribute charTermAttribute = fieldName.addAttribute(CharTermAttribute.class);
        try {
            fieldName.reset();
            while(fieldName.incrementToken()) {
                tokens.add(new SimpleToken(charTermAttribute.toString())
                        .setTokenString(charTermAttribute.toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokens;
    }

    @Override
    public Iterable<Token> tokenize(String input, Language language, StemMode stemMode, boolean removeAccents) {
        if (input.isEmpty()) return List.of();

        // Should we combine language + stemMode + removeAccents to make more variations available?
        return textToTokens(input, getAnalyzer(language));
    }
}
