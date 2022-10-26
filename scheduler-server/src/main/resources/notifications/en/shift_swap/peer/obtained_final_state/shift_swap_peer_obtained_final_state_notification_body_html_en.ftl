<#assign rshifts = receiverShifts?eval>
<#assign rscount = receiverShiftsCount?eval>
<#assign csn = choosen?eval>
<#assign status>${requestStatus}</#assign>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${receiverName}</title>
</head>
<body>
<p> Your request has been ${requestStatus} for the following shift swap: </p>
<#if receiverStatus == "APPROVED">
   <p> ${csn.shiftSkillName} on ${csn.shiftTeamName} </p>
   <p> on ${csn.shiftDate} ${csn.shiftStartTime}- ${csn.shiftEndTime} with: </p>
   <p> ${submitterSkillName} on ${submitterTeamName} </p>
   <p> on ${submitterShiftDate}  ${submitterShiftStartTime} - ${submitterShiftEndTime}</p>
<#else>
<p> target shift for ${submitterSkillName} on ${submitterTeamName} </p>
<p> on ${submitterShiftDate}  ${submitterShiftStartTime} - ${submitterShiftEndTime}</p>
</#if>
</body>
</html>