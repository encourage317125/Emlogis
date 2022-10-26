package com.emlogis.test.rest.structurelevel;

import com.emlogis.test.rest.BaseResourceTest;
import org.junit.Test;

import java.io.IOException;

public class SiteResourceTest extends BaseResourceTest {

    @Test
    public void testTextScenario() throws IOException {
        String text =
            "url|method|json|response_id|status\n" +
                    
            "sites|post|{'name':'SiteAA', 'description':'Site AA'}|#{site1}|\n" +
            /*"sites/#{site1}/absencetypes|post|{'name':'AT1', 'description':'AT1', 'hoursToDeduct':4}|#{at1}|\n" +
            "sites/#{site1}/absencetypes/#{at1}|delete|||\n" +
            "sites/#{site1}|delete|||\n" +
                    
            "sites|post|{'name':'SiteDD4', 'description':'Site CC'}|#{site2}\n" +
            "sites/#{site2}/absencetypes|post|{'name':'AT1', 'description':'AT1', 'hoursToDeduct':4}|#{at1}|\n" +
            "teams|post|{'siteId':'#{site2}', 'updateDto':{'name':'TeamA', 'description':'Team A'}}|#{teamA}|\n" +
            "employees|post|{'firstName':'Steve', 'lastName':'Martin', 'employeeIdentifier':'SteveMartin4'}|#{employee1}|\n" +
            "employees/#{employee1}/teams|post|{'teamId':'#{teamA}', 'isFloating':false, 'isHomeTeam':true, 'isSchedulable':true}||\n" +
            "employees/#{employee1}/teams/#{teamA}|put|{}||\n" +
            "employees/#{employee1}/cdavailability|post|{'absenceTypeId':'#{at1}', 'availabilityType':'Avail', 'reason':'Some reason'}||\n" +

            "sites/#{site2}/absencetypes/#{at1}|delete|||400\n" +*/
            "";

        text = text.replace("'", "\"");

        playTextScenario(text);
    }

}
