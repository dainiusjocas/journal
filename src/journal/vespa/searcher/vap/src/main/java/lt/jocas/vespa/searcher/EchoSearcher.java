package lt.jocas.vespa.searcher;

import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.result.Hit;
import com.yahoo.search.result.HitGroup;
import com.yahoo.search.searchchain.Execution;

public class EchoSearcher extends Searcher {
    @Override
    public Result search(Query query, Execution execution) {
        Result result = new Result(query);
        Hit hit = new Hit("echo");
        // Add all properties whose name starts with foo
        for (String propertyKey : query.properties().listProperties().keySet()) {
            if (propertyKey.startsWith("foo")) {
                hit.setField(propertyKey, query.properties().get(propertyKey));
            }
        }
        // Add properties from the query string
        hit.setField("URL_QUERY_STRING", query.getHttpRequest().propertyMap());
        // Add HTTP headers
        hit.setField("HTTP_HEADERS", query.getHttpRequest().getJDiscRequest().headers());
        // Add the artificial hit to the result
        HitGroup hits = new HitGroup();
        hits.add(hit);
        result.setHits(hits);
        // Add custom HTTP headers
        result.getHeaders(true).put("X-Foo-Header", "foo-value");
        return result;
    }
}
