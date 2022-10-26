package com.emlogis.test.rest.structurelevel;

import com.emlogis.test.rest.BaseResourceTest;
import org.junit.Test;

import java.io.IOException;

public class OrganizationResourceTest extends BaseResourceTest {

    @Test
    public void testFileScenario() throws IOException {
        playFileScenario("organization_test.csv");
    }

}
