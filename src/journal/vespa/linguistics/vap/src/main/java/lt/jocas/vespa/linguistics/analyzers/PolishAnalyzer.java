package lt.jocas.vespa.linguistics.analyzers;

import com.yahoo.container.di.componentgraph.Provider;
import org.apache.lucene.analysis.Analyzer;

public class LithuanianAnalyzer implements Provider<Analyzer> {
    @Override
    public Analyzer get() {
        return new org.apache.lucene.analysis.lt.LithuanianAnalyzer();
    }

    @Override
    public void deconstruct() {}
}
