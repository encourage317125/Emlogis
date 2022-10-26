<#assign status>${requestStatus}</#assign>
<#assign pshifts = peerShifts?eval>
<#assign pcount = peerCount?eval>
<#assign csn = choosen?eval>
Your request for this shift swap:
Swap ${submitterName} shift as ${submitterSkillName} on ${submitterTeamName}
on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime}
<#list ps as pshifts>
with ${ps.owner.name} shift as ${ps.shiftSkillName} on ${ps.shiftTeamName}
on ${ps.shiftDate} from ${ps.shiftStartTime}-${ps.shiftEndTime}
</#list>