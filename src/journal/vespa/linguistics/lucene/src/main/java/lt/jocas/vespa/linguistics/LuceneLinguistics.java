package lt.jocas.vespa.linguistics;

import com.google.inject.Inject;
import com.yahoo.language.Linguistics;
import com.yahoo.language.detect.Detector;
import com.yahoo.language.process.*;
import com.yahoo.language.simple.SimpleDetector;
import com.yahoo.language.simple.SimpleLinguistics;
import com.yahoo.language.simple.SimpleNormalizer;
import com.yahoo.language.simple.SimpleTransformer;

import java.util.ArrayList;

/**
 * Factory of Lucene based linguistics processor.
 *
 * TODO: docs for all available analysis components.
 * TODO: deploy vap and print all components in constructor or somewhere.
 * TODO: should I provide default settings for languages?
 */
public class LuceneLinguistics extends SimpleLinguistics {

    // Threadsafe instances
    private final Normalizer normalizer;
    private final Transformer transformer;
    private final Detector detector;
    private final CharacterClasses characterClasses;
    private final GramSplitter gramSplitter;
    private final Tokenizer tokenizer;

    private final Stemmer stemmer = (s, stemMode, language) -> {
        ArrayList<StemList> stemLists = new ArrayList<>();
        StemList word = new StemList();
        word.add(s);
        stemLists.add(word);
        return stemLists;
    };

    @Inject
    public LuceneLinguistics(LuceneAnalysisConfig config) {
        this.normalizer = new SimpleNormalizer();
        this.transformer = new SimpleTransformer();
        this.detector = new SimpleDetector();
        this.characterClasses = new CharacterClasses();
        this.gramSplitter = new GramSplitter(characterClasses);
        this.tokenizer = new LuceneTokenizer(config);
    }


    @Override
    public Stemmer getStemmer() {
        return stemmer;
    }

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public Normalizer getNormalizer() { return normalizer; }

    @Override
    public Transformer getTransformer() { return transformer; }

    @Override
    public Segmenter getSegmenter() { return new SegmenterImpl(getTokenizer()); }

    @Override
    public Detector getDetector() { return detector; }

    @Override
    public GramSplitter getGramSplitter() { return gramSplitter; }

    @Override
    public CharacterClasses getCharacterClasses() { return characterClasses; }

    @Override
    public boolean equals(Linguistics other) {
        // Check also equality of the LuceneAnalysisConfig
        return (other instanceof LuceneLinguistics); }
}
