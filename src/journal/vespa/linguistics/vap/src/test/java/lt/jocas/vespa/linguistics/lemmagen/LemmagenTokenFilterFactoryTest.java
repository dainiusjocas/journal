package lt.jocas.vespa.linguistics.lemmagen;

import com.yahoo.component.provider.ComponentRegistry;
import com.yahoo.config.FileReference;
import com.yahoo.language.Language;
import com.yahoo.language.process.StemMode;
import com.yahoo.language.process.Token;
import lt.jocas.vespa.linguistics.LuceneAnalysisConfig;
import lt.jocas.vespa.linguistics.LuceneLinguistics;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LemmagenTokenFilterFactoryTest {

    @Test
    public void testLemmagenTokenFilterFactory() {
        Language language = Language.SLOVAK;
        String languageCode = language.languageCode();
        LuceneAnalysisConfig enConfig = new LuceneAnalysisConfig.Builder()
                .configDir(FileReference.mockFileReferenceForUnitTesting(new File(".")))
                .analysis(
                        Map.of(languageCode,
                                new LuceneAnalysisConfig.Analysis.Builder().tokenFilters(List.of(
                                        new LuceneAnalysisConfig
                                                .Analysis
                                                .TokenFilters
                                                .Builder()
                                                .name("lemmagen")
                                                .conf("lexicon", "sk.lem"))))
                ).build();
        LuceneLinguistics linguistics = new LuceneLinguistics(enConfig, new ComponentRegistry<>());
        Iterable<Token> tokens = linguistics
                .getTokenizer()
                .tokenize("Mačky a psy", language, StemMode.ALL, false);
        assertEquals(List.of("Mačka", "a", "pes"), tokenStrings(tokens));
    }

    private List<String> tokenStrings(Iterable<Token> tokens) {
        List<String> tokenList = new ArrayList<>();
        tokens.forEach(token -> {
            tokenList.add(token.getTokenString());
        });
        return tokenList;
    }
}
