package com.emlogis.script.migration.data.proto

/**
 * Created by rjackson on 1/21/2015.
 */

String name = "Cecelia, . | \"CC\""

String fixedName = name.replaceAll("[^a-zA-Z0-9_-]+","_");
def email = " rjack2@gmail.com "
def email2 = "  "
if(!email?.trim()) {
    email = null
}
if(!email2?.trim()) {
    email2 = null
}
def emailPattern = ~/(?i)^([a-z0-9'_.-]+)@([\da-z.-]+)[.]([a-z.]{2,6})$/
boolean emailMatches =  emailPattern.matcher("Tom.Sybesma@jocogov.org").matches()

emailMatches =  emailPattern.matcher("robinson@co.rock.wi.us").matches()

emailMatches =  emailPattern.matcher("lianne@co.rock.wi.us").matches()

def loginPattern = ~/(?i)^[ a-z0-9'+,_.-]{3,60}[@]{0,1}[\da-z+_.-]*[.]*[a-z]{0,6}$/
def loginMatches = loginPattern.matcher("Humphrey, Erin").matches()