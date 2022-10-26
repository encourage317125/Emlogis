<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Dear ${recipientName}</title>
</head>
<body>
<h1>Request details</h1>

<p>At ${requestDate} ${originatorName} from team ${originatorTeam} (${tenant})
    created a ${notificationName} request for you!</p>

<p>Shift date: ${eventDate} </p>

<a href="${link}?&code=${code}&account=${recipientAccount}&tenant=${tenant}&decision=DENY"> DENY </a>
&nbsp;&nbsp;&nbsp;
<a href="${link}?&code=${code}&account=${recipientAccount}&tenant=${tenant}&decision=APPROVE"> APPROVE </a>
</body>
</html>