package lt.jocas.vespa.linguistics;

import com.yahoo.language.Language;
import com.yahoo.language.process.StemMode;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharFilterFactory;
import org.apache.lucene.analysis.TokenFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AnalyzerFactory {
    private static final Logger log = Logger.getLogger(AnalyzerFactory.class.getName());

    // Here list of current analyzers per language
    // the idea is not to create analyzers until they are needed
    // Analyzers are thread safe so no need to recreate them for every document
    private final LuceneAnalysisConfig config;

    private final Path configDir;

    private final Map<String, Analyzer> languageAnalyzers = new HashMap<>();

    private final Analyzer defaultAnalyzer = new StandardAnalyzer();

    private final static String STANDARD_TOKENIZER = "standard";

    public AnalyzerFactory(LuceneAnalysisConfig config) {
        this.config = config;
        this.configDir = config.configDir();
        log.info("Available char filters: " + CharFilterFactory.availableCharFilters());
        log.info("Available tokenizers: " + TokenFilterFactory.availableTokenFilters());
        log.info("Available tokenizers: " + TokenFilterFactory.availableTokenFilters());
    }

    public Analyzer getAnalyzer(Language language, StemMode stemMode, boolean removeAccents) {
        String analyzerKey = generateKey(language, stemMode, removeAccents);
        // if analyzer is configured, but instance is not created yet
        if (isConfiguredAndNotCreated(analyzerKey)) {
            Analyzer analyzer = setUpAnalyzer(analyzerKey);
            languageAnalyzers.put(analyzerKey, analyzer);
            return analyzer;
        } else {
            // Analyzer is already set up or it is not configured
            return languageAnalyzers.getOrDefault(analyzerKey, defaultAnalyzer);
        }
    }

    // Should we combine language + stemMode + removeAccents to make more variations possible?
    private String generateKey(Language language, StemMode stemMode, boolean removeAccents) {
        // default stem mode
        return language.languageCode();
    }

    private boolean isConfiguredAndNotCreated(String analyzerKey) {
        boolean isConfigured = null != config.analysis(analyzerKey);
        boolean isNotCreated = null == languageAnalyzers.get(analyzerKey);
        return (isConfigured && isNotCreated);
    }

    private Analyzer setUpAnalyzer(String analyzerKey) {
        try {
            LuceneAnalysisConfig.Analysis analysis = config.analysis(analyzerKey);
            CustomAnalyzer.Builder builder = CustomAnalyzer.builder(configDir);
            builder = withTokenizer(builder, analysis);
            builder = addCharFilters(builder, analysis);
            builder = addTokenFilters(builder, analysis);
            return builder.build();
        } catch (IOException e) {
            // TODO: what to Use as an analyzer in case resources are missing?
            // Definitely log WARNING
            // Since the resources should be in VAP, unit tests must catch the problem and prevent
            // VAP being deployed
            throw new RuntimeException(e);
        }
    }

    private CustomAnalyzer.Builder withTokenizer(CustomAnalyzer.Builder builder,
                                                 LuceneAnalysisConfig.Analysis analysis) throws IOException {
        if (null == analysis) {
            // By default we use the "standard" tokenizer
            return builder.withTokenizer(STANDARD_TOKENIZER, new HashMap<>());
        }
        String tokenizerName = analysis.tokenizer().name();
        Map<String, String> conf = analysis.tokenizer().conf();
        return builder.withTokenizer(tokenizerName, toModifiable(conf));
    }

    private CustomAnalyzer.Builder addCharFilters(CustomAnalyzer.Builder builder,
                                                  LuceneAnalysisConfig.Analysis analysis) throws IOException {
        if (null == analysis) {
            // by default there are no token filters
            return builder;
        }
        for (LuceneAnalysisConfig.Analysis.CharFilters charFilter : analysis.charFilters()) {
            builder.addCharFilter(charFilter.name(), toModifiable(charFilter.conf()));
        }
        return builder;
    }

    private CustomAnalyzer.Builder addTokenFilters(CustomAnalyzer.Builder builder,
                                                   LuceneAnalysisConfig.Analysis analysis) throws IOException {
        if (null == analysis) {
            // by default no token filters are added
            return builder;
        }
        for (LuceneAnalysisConfig.Analysis.TokenFilters tokenFilter : analysis.tokenFilters()) {
//            tokenFilter.conf().isEmpty() ? new HashMap<>() : new
            builder.addTokenFilter(tokenFilter.name(), toModifiable(tokenFilter.conf()));
        }
        return builder;
    }

    private Map<String, String> toModifiable(Map<String, String> map) {
        return new HashMap<>(map);
    }
}
