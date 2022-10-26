package com.emlogis.test.rest.tenant;

import com.emlogis.test.rest.BaseResourceTest;
import org.junit.Test;

import java.io.IOException;

public class AceResourceTest extends BaseResourceTest {

    @Test
    public void testTextScenario() throws IOException {
        String text =
                "url|method|json|response_id|status\n" +

                "sites|post|{'name':'SiteACE12'}|#{site1}|\n" +
                "teams|post|{'siteId':'#{site1}', 'updateDto':{'name':'TeamA'}}|#{teamA}\n" +
                "teams|post|{'siteId':'#{site1}', 'updateDto':{'name':'TeamB'}}|#{teamB}\n" +
                "teams|post|{'siteId':'#{site1}', 'updateDto':{'name':'TeamC'}}|#{teamC}\n" +
                "teams|post|{'siteId':'#{site1}', 'updateDto':{'name':'TeamD'}}|#{teamD}\n" +

                "roles/tacticaladminschedulerrole/aces|post|{'entityClass':'com.emlogis.model.structurelevel.Site', 'updateDto':{'permissions':[\"OrganizationProfileView\",\"OrganizationProfileMgmt\"]}}|#{aceA}\n" +
                "roles/tacticaladminschedulerrole/aces|post|{'entityClass':'com.emlogis.model.structurelevel.Team', 'updateDto':{'permissions':['OrganizationProfileView','OrganizationProfileMgmt'], 'pattern':'.*/teamA', 'negate':'true'}}|#{aceA}\n" +
                "roles/tacticaladminschedulerrole/aces|post|{'entityClass':'com.emlogis.model.structurelevel.Team', 'updateDto':{'permissions':['OrganizationProfileView','OrganizationProfileMgmt'], 'pattern':'.*/teamC', 'negate':'true'}}|#{aceC}\n" +

                "sites/#{site1}/teams|get||\n" +

//                "sites/#{site1}|delete|||\n" +
                "";

        text = text.replace("'", "\"");

        playTextScenario(text);
    }
}