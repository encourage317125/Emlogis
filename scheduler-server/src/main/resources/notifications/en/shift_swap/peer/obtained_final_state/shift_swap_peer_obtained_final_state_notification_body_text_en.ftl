<#assign rshifts = receiverShifts?eval>
<#assign rscount = receiverShiftsCount?eval>
<#assign csn = choosen?eval>
Your request has been ${requestStatus} for the following shift swap:
<#if receiverStatus == "APPROVED">
${csn.shiftSkillName} on ${csn.shiftTeamName}
on ${csn.shiftDate} ${csn.shiftStartTime}- ${csn.shiftEndTime} with:
${submitterSkillName} on ${submitterTeamName}
on ${submitterShiftDate}  ${submitterShiftStartTime} - ${submitterShiftEndTime}
<#else>
target shift for ${submitterSkillName} on ${submitterTeamName}
on ${submitterShiftDate}  ${submitterShiftStartTime} - ${submitterShiftEndTime}
</#if>