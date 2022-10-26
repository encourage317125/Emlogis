<#assign pshifts = peerShifts?eval>
<#assign pcount = peerCount?eval>
A request has been received for the following shift swap: Swap ${submitterName} shift as ${submitterSkillName}
on ${submitterTeamName} on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime}
<#if pcount == 1>
 with ${peerName} shift as ${peerSkillName} on ${peerTeamName} on ${peerShiftDate} from ${peerShiftStartTime}-${peerShiftEndTime}.
<#else>
with:
    <#list ps as pshifts>
        with ${peerName} shift as ${ps.peerSkillName} on ${ps.peerTeamName} on ${ps.peerShiftDate} from ${ps.peerShiftStartTime}-${ps.peerShiftEndTime}.
    </#list>
</#if>
To approve, reply Yes, To reject, reply No. Do not change the email subject.