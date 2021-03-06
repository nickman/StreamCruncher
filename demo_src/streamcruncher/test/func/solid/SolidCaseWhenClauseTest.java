package streamcruncher.test.func.solid;

import java.util.List;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import streamcruncher.test.TestGroupNames;
import streamcruncher.test.func.BatchResult;
import streamcruncher.test.func.generic.CaseWhenClauseTest;

/*
 * Author: Ashwin Jayaprakash Date: Jan 22, 2007 Time: 12:40:52 PM
 */

public class SolidCaseWhenClauseTest extends CaseWhenClauseTest {
    @Override
    @BeforeGroups(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, value = { TestGroupNames.SC_TEST_SOLID }, groups = { TestGroupNames.SC_TEST_SOLID })
    public void init() throws Exception {
        super.init();
    }

    @Test(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, groups = { TestGroupNames.SC_TEST_SOLID })
    protected void performTest() throws Exception {
        List<BatchResult> results = test();
    }

    @Override
    @AfterGroups(dependsOnGroups = { TestGroupNames.SC_INIT_REQUIRED }, value = { TestGroupNames.SC_TEST_SOLID }, groups = { TestGroupNames.SC_TEST_SOLID })
    public void discard() {
        super.discard();
    }
}
