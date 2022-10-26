<#assign status>${requestStatus}</#assign>
<#assign pcount = peerCount?eval>
<#assign csn = choosen?eval>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${receiverName}</title>
</head>
<body>

<#if status == "APPROVED">
<p> Request has been approved for the following shift WIP: </p>
<p> ${csn.name} assigned as ${submitterSkillName} on ${submitterTeamName} on ${submitterShiftDate}  </p>
<p> from ${submitterShiftEndTime} - ${submitterShiftEndTime}. </p>
<#else>
<p> Request for the following shift wip: </p>
<p> ${submitterName} shift as ${submitterSkillName} on ${submitterTeamName} </p>
<p> ${submitterShiftDate} ${submitterShiftStartTime} - ${submitterShiftEndTime} (${pcount} options): </p>
<p> has been ${requestStatus}. </p>
</#if>

</body>
</html>