<#assign status>${requestStatus}</#assign>
<#assign pcount = peerCount?eval>
<#assign csn = choosen?eval>
<#if status == "APPROVED">
Request has been approved for the following shift WIP:
${submitterSkillName} on ${submitterTeamName} on ${submitterShiftStartDate}
from ${submitterShiftEndDateTime} - ${submitterShiftEndDateTime} with :
with ${csn.name}.
<#else>
Request has been ${requestStatus} for shift swap:
${submitterName}'s shift as ${submitterSkillName} on ${submitterTeamName}
on ${submitterShiftDate} from ${submitterShiftStartTime} - ${submitterShiftEndTime} (${pcount} options).
</#if>