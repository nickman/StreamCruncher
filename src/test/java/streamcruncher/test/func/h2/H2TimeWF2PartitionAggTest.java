package streamcruncher.test.func.h2;

import java.util.List;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import streamcruncher.test.TestGroupNames;
import streamcruncher.test.func.BatchResult;
import streamcruncher.test.func.generic.TimeWF2PartitionAggTest;

/*
 * Author: Ashwin Jayaprakash Date: Oct 3, 2006 Time: 10:55:32 PM
 */

public class H2TimeWF2PartitionAggTest extends TimeWF2PartitionAggTest {
    @Override
    @BeforeGroups(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, value = { TestGroupNames.SC_TEST_H2 }, groups = { TestGroupNames.SC_TEST_H2 })
    public void init() throws Exception {
        super.init();
    }

    @Test(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, groups = { TestGroupNames.SC_TEST_H2 })
    protected void performTest() throws Exception {
        List<BatchResult> results = test();
    }

    @Override
    @AfterGroups(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, value = { TestGroupNames.SC_TEST_H2 }, groups = { TestGroupNames.SC_TEST_H2 })
    public void discard() {
        super.discard();
    }
}
