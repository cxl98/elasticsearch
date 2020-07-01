/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ml.dataframe.evaluation.regression;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.xpack.core.ml.dataframe.evaluation.EvaluationMetricResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.elasticsearch.xpack.core.ml.dataframe.evaluation.MockAggregations.mockSingleValue;
import static org.hamcrest.Matchers.equalTo;

public class PseudoHuberTests extends AbstractSerializingTestCase<PseudoHuber> {

    @Override
    protected PseudoHuber doParseInstance(XContentParser parser) throws IOException {
        return PseudoHuber.fromXContent(parser);
    }

    @Override
    protected PseudoHuber createTestInstance() {
        return createRandom();
    }

    @Override
    protected Writeable.Reader<PseudoHuber> instanceReader() {
        return PseudoHuber::new;
    }

    public static PseudoHuber createRandom() {
        return new PseudoHuber(randomBoolean() ? randomDoubleBetween(0.0, 1000.0, false) : null);
    }

    public void testEvaluate() {
        Aggregations aggs = new Aggregations(Arrays.asList(
            mockSingleValue("regression_pseudo_huber", 0.8123),
            mockSingleValue("some_other_single_metric_agg", 0.2377)
        ));

        PseudoHuber pseudoHuber = new PseudoHuber((Double) null);
        pseudoHuber.process(aggs);

        EvaluationMetricResult result = pseudoHuber.getResult().get();
        String expected = "{\"value\":0.8123}";
        assertThat(Strings.toString(result), equalTo(expected));
    }

    public void testEvaluate_GivenMissingAggs() {
        Aggregations aggs = new Aggregations(Collections.singletonList(
            mockSingleValue("some_other_single_metric_agg", 0.2377)
        ));

        PseudoHuber pseudoHuber = new PseudoHuber((Double) null);
        pseudoHuber.process(aggs);

        EvaluationMetricResult result = pseudoHuber.getResult().get();
        assertThat(result, equalTo(new PseudoHuber.Result(0.0)));
    }
}
