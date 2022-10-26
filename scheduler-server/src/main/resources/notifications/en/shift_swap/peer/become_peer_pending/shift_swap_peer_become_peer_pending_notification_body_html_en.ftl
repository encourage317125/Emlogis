<#assign rshifts = receiverShifts?eval>
<#assign rscount = receiverShiftsCount?eval>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${receiverName}</title>
</head>
<body>

<p>   Swap ${submitterName} shift as ${submitterSkillName} on ${submitterTeamName}  </p>
<p> on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime}  </p>
<#if rscount == 1>
    <#list rshifts as rshift>
      <p> with ${rshift.owner.name} shift as ${rshift.shiftSkillName} on ${rshift.shiftTeamName} </p>
      <p> on ${rshift.shiftDate} from ${rshift.shiftStartTime}-${rshift.shiftEndTime} </p>
    </#list>
<#else>
   <ul>
       <li>with ${rshift.owner.name} shift as ${rshift.shiftSkillName} on ${rshift.shiftTeamName} on ${rshift.shiftDate} from ${rshift.shiftStartTime}-${rshift.shiftEndTime}</li>
   </ul>
</#if>
<p> is pending for your approval. </p>
<p> To approve, reply Yes, To reject, reply No. Do not change the email subject. </p>

</body>
</html>