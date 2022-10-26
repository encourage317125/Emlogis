package com.emlogis.test.rest.schedule;

import com.emlogis.test.rest.BaseResourceTest;
import org.junit.Test;

import java.io.IOException;

public class ShiftReqOldTest extends BaseResourceTest {

    final static private String SHIFT_REQ_ADD_RECORD = "shiftstructures/%s/shiftreqs|post|{'startTime':8,'durationInMins':240,'employeeCount':1,'dayIndex':1,'skillProficiencyLevel':1,'skillId':'%s','shiftLengthId':'%s'}|\n";

    @Test
    public void testTextScenario() throws IOException {
        final String shiftStructureId = "1zkj885cgueu2j0pjy41t";
        final String skillId = "1zkj79u5v15flv0dj8e0x";
        final String shiftLengthId = "1zkj78xfkdvg0y43jeku9";

        String text =
            "url|method|json|response_id|status\n";

             for (int i = 0; i < 10000; i++) {
                 text += String.format(SHIFT_REQ_ADD_RECORD, shiftStructureId, skillId, shiftLengthId);
             }

        text += "";

        text = text.replace("'", "\"");

        long start = System.currentTimeMillis();
        playTextScenario(text);
        System.out.println((System.currentTimeMillis() - start) + " ms");
    }

}
