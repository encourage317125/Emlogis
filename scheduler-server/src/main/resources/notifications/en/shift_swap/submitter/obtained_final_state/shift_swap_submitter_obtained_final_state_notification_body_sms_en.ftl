<#assign status>${requestStatus}</#assign>
<#assign pshifts = peerShifts?eval>
<#assign pcount = peerCount?eval>
<#assign csn = choosen?eval>
Your request has been ${status} for the following shift swap:
Swap ${submitterName} shift as ${submitterSkillName}on ${submitterTeamName} on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime}
<#if status == "APPROVED">
with ${csn.owner.name} as ${csn.shiftSkillName} on ${csn.shiftTeamName} on ${csn.shiftDate} ${csn.shiftStartTime} - ${csn.shiftEndTime}.
<#else>
    <#list pshifts as pshift>
    ${pshift.owner.name} as ${pshift.shiftSkillName} on ${pshift.shiftTeamName} on ${pshift.shiftDate} ${pshift.shiftStartTime} - ${pshift.shiftEndTime}.
    </#list>
</#if>