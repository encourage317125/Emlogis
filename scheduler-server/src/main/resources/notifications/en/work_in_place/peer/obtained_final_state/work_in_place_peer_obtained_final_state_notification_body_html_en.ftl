<#assign rs = receiver?eval>
<#assign status>${receiverStatus}</#assign>
<#assign csn = choosen?eval>
<#assign pcount = peerCount?eval>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${receiverName}</title>
</head>
<body>

<#if status == "APPROVED">
<p> Your request for the following WIP has been approved. You will work as: </p>
<p> ${submitterSkillName} on ${submitterTeamName} </p>
<p> on ${submitterShiftDate} ${submitterShiftStartTime} - ${submitterShiftEndTime} </p>
<p> in place of ${submitterName} </p>
<#else>
<p> Your request for the following WIP: </p>
<p> ${submitterName}'s shift as ${submitterSkillName} on ${submitterTeamName} </p>
<p> ${submitterShiftDate} ${submitterShiftStartTime} - ${submitterShiftEndTime} (${pcount} options): </p>
<p> has been ${requestStatus}  </p>
</#if>
</body>
</html>