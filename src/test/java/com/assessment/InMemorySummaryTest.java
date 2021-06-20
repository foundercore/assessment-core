package com.assessment;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.assessment.common.InMemorySummary;
import com.assessment.common.InMemorySummaryProcessor;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InMemorySummaryTest {

    @Test
    @DisplayName("In memory summary creation should be successful")
    @Order(1)
    public void verifyInMemorySummaryCreationSuccess() {
        InMemorySummary e1 = new InMemorySummary();
        e1.setName("m1");
        e1.setDimensionValue("k1", "1");
        e1.setDimensionValue("k2", "2");
        e1.setMeasureValue("m1", 1d);
        e1.setMeasureValue("m2", 2d);

        InMemorySummary e2 = new InMemorySummary();
        e2.setName("m1");
        e2.setDimensionValue("k1", "1");
        e2.setDimensionValue("k2", "2");
        e2.setMeasureValue("m1", 1d);
        e2.setMeasureValue("m2", 2d);

        InMemorySummaryProcessor processor = new InMemorySummaryProcessor();
        processor.add(e1);
        processor.add(e2);
        InMemorySummary summary = processor.getByName("m1").iterator().next();
        assert summary.getMeasures().get("m1") == 2;
        assert summary.getMeasures().get("m2") == 4;
    }
}
