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
<p> Your request for this shift swap: </p>
<p> Swap ${submitterName} shift as ${submitterSkillName} on ${submitterTeamName} </p>
<p> on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime} </p>
<#if pcount == 1>
    <#list ps as pshifts>
    <p> with ${ps.owner.name} shift as ${ps.shiftSkillName} on ${ps.shiftTeamName} </p>
    <p> on ${ps.shiftDate} from ${ps.shiftStartTime}-${ps.shiftEndTime} </p>
    </#list>
<#else>
 <ul>
     <li>
         <p> with ${ps.owner.name} shift as ${ps.shiftSkillName} on ${ps.shiftTeamName} </p>
         <p> on ${ps.shiftDate} from ${ps.shiftStartTime}-${ps.shiftEndTime} </p>
     </li>
 </ul>
</#if>
<p> is pending peer and administrator approval. </p>
</body>
</html>