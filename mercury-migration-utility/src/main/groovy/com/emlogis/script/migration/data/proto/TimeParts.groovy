package com.emlogis.script.migration.data.proto

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * Created by rjackson on 7/8/2015.
 */

DateTime timeOfDay = new DateTime(1436390357671);

String month = timeOfDay.monthOfYear().toString();
String dateString = timeOfDay.dayOfMonth().toString();
String year = timeOfDay.year().toString();

String hourOfDay = timeOfDay.hourOfDay();

DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");

String timeString = dtf.print(timeOfDay)

DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("hh:mm a");

String hourString = hourFormatter.print(timeOfDay)

def test = "test"

