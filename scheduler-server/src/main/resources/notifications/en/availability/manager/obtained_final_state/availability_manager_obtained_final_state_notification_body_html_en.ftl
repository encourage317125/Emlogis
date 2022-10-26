<#assign subtype>${subtype}</#assign>
<#assign status>${requestStatus}</#assign>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
</head>
<body>
<#if subtype == "AvailcalUpdateParamsCDPrefDto">
  <p> The request to change availability for ${submitterName} for ${weekDay} </p>
  <p> to ${startTime} - ${endTIme} until ${effectiveUntilDate} </p>
  <p> has been ${requestStatus}. </p>
</#if>
<#if subtype == "AvailcalUpdateParamsCDAvailDto">
 <p> The request to change availability for ${submitterName} for ${weekDay} </p>
 <p> to ${startTime} - ${endTIme} until ${effectiveUntilDate} </p>
 <p> has been ${requestStatus}. </p>
</#if>
<#if subtype == "AvailcalUpdateParamsCIPrefDto">
  <p> The request to change availability for ${submitterName} for ${weekDay} </p>
  <p> to ${startTime} - ${endTIme} until ${effectiveUntilDate} </p>
  <p> has been ${requestStatus}. </p>
</#if>
<#if subtype == "AvailcalUpdateParamsCIAvailDto">
  <p> The request to change availability for ${submitterName} for ${weekDay} </p>
  <p> to ${startTime} - ${endTIme} until ${effectiveUntilDate} </p>
  <p> has been ${requestStatus}. </p>
</#if>
</body>
</html>