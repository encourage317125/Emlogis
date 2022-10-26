<#assign subtype>${subtype}</#assign>
<#assign status>${requestStatus}</#assign>
<#if subtype == "AvailcalUpdateParamsCDPrefDto">
The request to change availability for ${submitterName} for ${weekDay}
to ${startTime} - ${endTIme} until ${effectiveUntilDate}
has been ${requestStatus}.
</#if>
<#if subtype == "AvailcalUpdateParamsCDAvailDto">
The request to change availability for ${submitterName} for ${weekDay}
to ${startTime} - ${endTIme} until ${effectiveUntilDate}
has been ${requestStatus}.
</#if>
<#if subtype == "AvailcalUpdateParamsCIPrefDto">
The request to change availability for ${submitterName} for ${weekDay}
to ${startTime} - ${endTIme} until ${effectiveUntilDate}
has been ${requestStatus}.
</#if>
<#if subtype == "AvailcalUpdateParamsCIAvailDto">
The request to change availability for ${submitterName} for ${weekDay}
to ${startTime} - ${endTIme} until ${effectiveUntilDate}
has been ${requestStatus}.
</#if>