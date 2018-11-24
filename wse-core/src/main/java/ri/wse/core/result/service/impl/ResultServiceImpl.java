package ri.wse.core.result.service.impl;

import org.springframework.stereotype.Service;
import ri.wse.core.result.service.ResultService;
import ri.wse.model.QueryResult;
import ri.wse.queryProcessor.QueryProcessor;

@Service("resultService")
public class ResultServiceImpl implements ResultService {


    @Override
    public QueryResult getResults(String query) {
        QueryProcessor queryProcessor = new QueryProcessor(query);
        return new QueryResult(queryProcessor.manageQuery());
    }

}
