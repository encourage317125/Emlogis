<#assign status>${requestStatus}</#assign>
<#assign ps = peers?eval>
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
<p> ${submitterSkillName} on ${submitterTeamName} on ${submitterShiftStartDate} </p>
<p> from ${submitterShiftEndDateTime} - ${submitterShiftEndDateTime} with :</p>
<p> with ${csn.name}.</p>
<#else>
<p> Request has been ${requestStatus} for shift swap: </p>
<p> ${submitterName}'s shift as ${submitterSkillName} on ${submitterTeamName} </p>
<p> on ${submitterShiftDate} from ${submitterShiftStartTime} - ${submitterShiftEndTime} (${pcount} options). </p>
</#if>

</body>
</html>