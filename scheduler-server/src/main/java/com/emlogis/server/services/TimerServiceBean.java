package com.emlogis.server.services;

import com.emlogis.common.services.tenant.ACEService;
import com.emlogis.model.ACE;
import org.joda.time.DateTime;

import javax.ejb.*;
import java.util.List;

@Startup
@Singleton
@DependsOn("SchedulingResponseServiceBean")
public class TimerServiceBean implements TimerService {

    private final static long TWO_WEEKS = 14L * 24 * 60 * 60 * 1000;

    @EJB
    private ACEService aceService;

    public TimerServiceBean() {}

    @Schedule(second = "00", minute = "00", hour = "00", persistent = false)
    public void voidNotMatchingACEs() {
        long currentTime = System.currentTimeMillis();

        List<ACE> unlinkedACEs = aceService.getUnlinkedACEs();
        for (ACE ace : unlinkedACEs) {
            if (ace.getTagged() == null) {
                ace.setTagged(new DateTime(currentTime));
                aceService.update(ace);
            } else {
                if (currentTime - ace.getTagged().getMillis() >= TWO_WEEKS) {
                    aceService.delete(ace);
                }
            }
        }
    }

}
