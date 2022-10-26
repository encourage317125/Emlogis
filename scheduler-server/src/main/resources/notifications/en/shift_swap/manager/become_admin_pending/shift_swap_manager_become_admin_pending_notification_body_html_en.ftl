<#assign pshifts = peerShifts?eval>
<#assign pcount = peerCount?eval>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${receiverName}</title>
</head>
<body>
<p> A request has been received for the following shift swap: Swap ${submitterName} shift as ${submitterSkillName} </p>
<p> on ${submitterTeamName} on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime} </p>
<#if pcount == 1>
 <p> with ${peerName} shift as ${peerSkillName} on ${peerTeamName} on ${peerShiftDate} from ${peerShiftStartTime}-${peerShiftEndTime}. </p>
<#else>
    <p> with: </p>
    <#list ps as pshifts>
    <ul>
     <li> with ${peerName} shift as ${ps.peerSkillName} on ${ps.peerTeamName} on ${ps.peerShiftDate} from ${ps.peerShiftStartTime}-${ps.peerShiftEndTime}. </li>
    </ul>
    </#list>
</#if>
<p> To approve, reply Yes, To reject, reply No. Do not change the email subject. </p>
</body>
</html>