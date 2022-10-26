<#assign rs = receiver?eval>
<#assign status>${receiverStatus}</#assign>
<#assign csn = choosen?eval>
<#assign pcount = peerCount?eval>
<#if status == "APPROVED">
Your request for the following WIP has been approved. You will work as:
${submitterSkillName} on ${submitterTeamName}
on ${submitterShiftDate} ${submitterShiftStartTime} - ${submitterShiftEndTime}
in place of ${submitterName}
<#else>
Your request for the following WIP:
${submitterName}'s shift as ${submitterSkillName} on ${submitterTeamName}
${submitterShiftDate} ${submitterShiftStartTime} - ${submitterShiftEndTime} (${pcount} options):
has been ${requestStatus}
</#if>