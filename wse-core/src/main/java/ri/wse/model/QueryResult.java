package ri.wse.model;

import javafx.util.Pair;

import java.util.List;

public class QueryResult {

    private List<Pair<String, String>> queryResults;

    public QueryResult(List<Pair<String, String>> queryResults) {
        this.queryResults = queryResults;
    }

    public List<Pair<String, String>> getQueryResults() {
        return queryResults;
    }

    public void setQueryResults(List<Pair<String, String>> queryResults) {
        this.queryResults = queryResults;
    }
}
