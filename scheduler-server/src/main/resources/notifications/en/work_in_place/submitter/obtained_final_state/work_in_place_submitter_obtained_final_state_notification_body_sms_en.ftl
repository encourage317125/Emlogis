<#assign status>${requestStatus}</#assign>
<#assign pcount = peerCount?eval>
<#assign csn = choosen?eval>
<#if status == "APPROVED">
Request has been approved for the following shift WIP:
${csn.name} assigned as ${submitterSkillName} on ${submitterTeamName} on ${submitterShiftDate}
from ${submitterShiftEndTime} - ${submitterShiftEndTime}.
<#else>
Request for the following shift wip:
${submitterName} shift as ${submitterSkillName} on ${submitterTeamName}
${submitterShiftDate} ${submitterShiftStartTime} - ${submitterShiftEndTime} (${pcount} options):
has been ${requestStatus}.
</#if>