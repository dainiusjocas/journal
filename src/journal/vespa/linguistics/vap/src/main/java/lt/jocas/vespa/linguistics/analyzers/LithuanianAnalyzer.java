package lt.jocas.vespa.linguistics.analyzers;

import com.yahoo.container.di.componentgraph.Provider;
import org.apache.lucene.analysis.Analyzer;

public class StandardAnalyzer implements Provider<Analyzer> {
    @Override
    public Analyzer get() {
        return new org.apache.lucene.analysis.standard.StandardAnalyzer();
    }

    @Override
    public void deconstruct() {}
}
