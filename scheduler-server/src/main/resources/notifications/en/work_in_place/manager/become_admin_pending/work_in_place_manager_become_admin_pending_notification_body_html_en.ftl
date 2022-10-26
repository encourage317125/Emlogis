<#assign status>${requestStatus}</#assign>
<#assign csn = choosen?eval>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${receiverName}</title>
</head>
<body>

<p>   A WIP request has been received for the shift of ${submitterName} </p>
<p> as ${submitterSkillName} on ${submitterTeamName}  on ${submitterShiftDate} </p>
<p> from ${submitterShiftStartTime} - ${submitterShiftEndTime} (${peerCount} options). </p>

To approve, reply Yes, To reject, reply No. Do not change the email subject.
</body>
</html>