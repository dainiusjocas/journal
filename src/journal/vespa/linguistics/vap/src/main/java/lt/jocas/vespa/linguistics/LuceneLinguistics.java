package lt.jocas.vespa.linguistics;

import com.google.inject.Inject;
import com.yahoo.language.Linguistics;
import com.yahoo.language.process.Tokenizer;
import com.yahoo.language.simple.SimpleLinguistics;

public class LuceneLinguistics extends SimpleLinguistics {

    private final LuceneTokenizer tokenizer;

    @Inject
    public LuceneLinguistics(LuceneAnalysisConfig config) {
        tokenizer = new LuceneTokenizer(config);
    }

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public boolean equals(Linguistics other) { return (other instanceof LuceneLinguistics); }
}
