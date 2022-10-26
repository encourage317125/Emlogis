package com.emlogis.test.rest.shiftpattern;

import com.emlogis.test.rest.BaseResourceTest;
import org.junit.Test;

import java.io.IOException;

public class ShiftPatternResourceTest extends BaseResourceTest {

    @Test
    public void testTextScenario() throws IOException {
        String text =
                "url|method|json|response_id|status\n" +

                "shiftlengths|post|{'name':'sl1', 'updateDto':{'lengthInMin':'60', 'paidTimeInMin':'60'}}|#{sl1}|\n" +
                "shiftlengths|post|{'name':'sl2', 'updateDto':{'lengthInMin':'120', 'paidTimeInMin':'120'}}|#{sl2}|\n" +

                "shifttypes|post|{'shiftLengthId':'#{sl1}', 'updateDto':{'name':'st1'}}|#{st1}|\n" +
                "shifttypes|post|{'shiftLengthId':'#{sl1}', 'updateDto':{'name':'st2'}}|#{st2}|\n" +

                "shiftpatterns|post|{'name':'q1', 'skillId':'Nurse', 'teamId':'West', 'updateDto':{'cdDate':11111}}|#{sp1}|\n" +
                "shiftpatterns|post|{'name':'q2', 'skillId':'Nurse', 'teamId':'East', 'updateDto':{'cdDate':11111}}}|#{sp2}|\n" +

                "shiftpatterns/#{sp2}|put|{'cdDate':11111, 'shiftDemandDtos':[" +
                        "{'employeeCount':2, 'lengthInMin':30}, " +
                        "{'employeeCount':1}, " +
                        "{'lengthInMin':20}" +
                        "]}|#{sr1}|\n" +
                "shiftpatterns/#{sp2}|put|{'cdDate':11111, 'shiftReqDtos':[" +
                        "{'employeeCount':2, 'excessCount':3, 'shiftTypeId': '#{st1}'}, " +
                        "{'employeeCount':1, 'shiftTypeId': '#{st2}'}, " +
                        "{'excessCount':2, 'shiftTypeId': '#{st1}'}" +
                        "]}|#{sr1}|\n" +
                "";

        text = text.replace("'", "\"");

        playTextScenario(text);
    }

    @Test
    public void testFileScenario() throws IOException {
        playFileScenario("shift_pattern_test.csv");
    }
}
