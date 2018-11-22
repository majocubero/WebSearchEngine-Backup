package ri.wse.core.result.impl;

import org.springframework.stereotype.Service;
import ri.wse.core.result.ResultService;

@Service("resultService")
public class ResultServiceImpl implements ResultService{

    @Override
    public String sayHello() {
        return "Hello";
    }
}
