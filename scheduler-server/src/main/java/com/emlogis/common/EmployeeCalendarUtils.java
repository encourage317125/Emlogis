package com.emlogis.common;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.*;
import org.apache.commons.lang3.time.DateUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmployeeCalendarUtils {

    public static class CalendarEvent {

        private String id;
        private Date start;
        private Date end;
        private String summary;
        private int sequence = 0;
        private String organizer;
        private String description;
        private boolean deleted = false;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public String getOrganizer() {
            return organizer;
        }

        public void setOrganizer(String organizer) {
            this.organizer = organizer;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }
    }

    @SuppressWarnings("unchecked")
    public static net.fortuna.ical4j.model.Calendar createICalendar(List<CalendarEvent> tevs)
            throws URISyntaxException {
        if (tevs == null || tevs.size() == 0) {
            return null;
        }

        final net.fortuna.ical4j.model.Calendar iCal = new net.fortuna.ical4j.model.Calendar();

        // Currently, we don't store time zone per team or employees Therefore,
        // use server's time zone as the default
        Calendar calendar = Calendar.getInstance();
        java.util.TimeZone serverTimeZone = calendar.getTimeZone();
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        VTimeZone vtz = registry.getTimeZone(serverTimeZone.getID()).getVTimeZone();

        // Somehow, the default VTimeZone has too many DayLight & Standard
        // components. We only need one Daylight & Standard section
        Component dl = vtz.getObservances().getComponent("DAYLIGHT");
        Component sd = vtz.getObservances().getComponent("STANDARD");
        vtz.getObservances().clear();
        vtz.getObservances().add(dl);
        vtz.getObservances().add(sd);

        // Set default ICal components
        iCal.getComponents().add(vtz);
        iCal.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
        iCal.getProperties().add(Version.VERSION_2_0);
        iCal.getProperties().add(CalScale.GREGORIAN);
        iCal.getProperties().add(Method.PUBLISH);

        final List<VEvent> events = new ArrayList<>();
        for (CalendarEvent tev : tevs) {

            VEvent ev = createVEvent(tev);
            if (ev != null) {
                events.add(ev);
            }
        }
        iCal.getComponents().addAll(events);

        return iCal;
    }

    public static VEvent createVEvent(CalendarEvent event) throws URISyntaxException {
        VEvent vEvent;

        // do not create an event for deleted events (URL synch does not need
        // it)
        if (event.isDeleted()) {
            return null;
        }

        if (event.getEnd() == null) { // All-day event
            vEvent = new VEvent(new net.fortuna.ical4j.model.Date(event.getStart()),
                    new net.fortuna.ical4j.model.Date(DateUtils.addDays(event.getStart(), 1)), event.getSummary());
        } else {
            DateTime start = new net.fortuna.ical4j.model.DateTime(event.getStart());
            start.setUtc(true);

            DateTime end = new net.fortuna.ical4j.model.DateTime(event.getEnd());
            end.setUtc(true);

            vEvent = new VEvent(start, end, event.getSummary());
        }

        vEvent.getProperties().add(new Sequence(event.getSequence()));

        if (event.getOrganizer() != null) {
            vEvent.getProperties().add(new Organizer(event.getOrganizer()));
        }

        if (event.getDescription() != null) {
            vEvent.getProperties().add(new Description(event.getDescription()));
        }

        vEvent.getProperties().add(new Uid(event.getId()));

        return vEvent;
    }

}
