<#assign status>${requestStatus}</#assign>
<#assign pshifts = peerShifts?eval>
<#assign pcount = peerCount?eval>
<#assign csn = choosen?eval>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${receiverName}</title>
</head>
<body>
<p> Your request has been ${status} for the following shift swap: </p>
<p> Swap ${submitterName} shift as ${submitterSkillName}on ${submitterTeamName} on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime}</p>
<#if status == "APPROVED">
<p> with ${csn.owner.name} as ${csn.shiftSkillName} on ${csn.shiftTeamName} on ${csn.shiftDate} ${csn.shiftStartTime} - ${csn.shiftEndTime}.</p>
<#else>
    <#list pshifts as pshift>
    <p> ${pshift.owner.name} as ${pshift.shiftSkillName} on ${pshift.shiftTeamName} on ${pshift.shiftDate} ${pshift.shiftStartTime} - ${pshift.shiftEndTime}. </p>
    </#list>
</#if>


</body>
</html>