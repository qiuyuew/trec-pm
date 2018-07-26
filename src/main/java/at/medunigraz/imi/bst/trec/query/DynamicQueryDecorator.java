package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import joptsimple.internal.Strings;

import java.io.File;
import java.util.*;

public abstract class DynamicQueryDecorator extends QueryDecorator {

    public DynamicQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public List<Result> query(Topic topic) {
        topic = expandTopic(topic);
        return decoratedQuery.query(topic);
    }

    public abstract Topic expandTopic(Topic topic);
}
