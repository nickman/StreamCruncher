package streamcruncher.test.func.derby;

import java.util.List;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import streamcruncher.test.TestGroupNames;
import streamcruncher.test.func.BatchResult;
import streamcruncher.test.func.generic.OAFJoinTest;

/*
 * Author: Ashwin Jayaprakash Date: Oct 15, 2006 Time: 2:34:17 PM
 */

public class DerbyOAFJoinTest extends OAFJoinTest {
    @Override
    @BeforeGroups(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, value = { TestGroupNames.SC_TEST_DERBY }, groups = { TestGroupNames.SC_TEST_DERBY })
    public void init() throws Exception {
        super.init();
    }

    @Test(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, groups = { TestGroupNames.SC_TEST_DERBY })
    protected void performTest() throws Exception {
        List<BatchResult> results = test();
    }

    @Override
    @AfterGroups(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, value = { TestGroupNames.SC_TEST_DERBY }, groups = { TestGroupNames.SC_TEST_DERBY })
    public void discard() {
        super.discard();
    }
}
