package lt.jocas.vespa.linguistics;

import com.yahoo.language.Language;
import com.yahoo.language.process.StemMode;
import com.yahoo.language.process.Token;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LuceneTokenizerTest {

    @Test
    public void testTokenizer() {
        String text = "This is my Text";
        var tokenizer = new LuceneTokenizer(
                new LuceneAnalysisConfig.Builder()
                        .build());
        Iterator<Token> tokens = tokenizer
                .tokenize(text, Language.ENGLISH, StemMode.ALL, true)
                .iterator();
        assertToken("This", tokens);
        assertToken("is", tokens);
        assertToken("my", tokens);
        assertToken("Text", tokens);
    }

    private void assertToken(String tokenString, Iterator<Token> tokens) {
        Token t = tokens.next();
        assertEquals(tokenString, t.getTokenString());
    }
}
