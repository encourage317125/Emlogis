<#assign rshifts = receiverShifts?eval>
<#assign rscount = receiverShiftsCount?eval>
Swap ${submitterName} shift as ${submitterSkillName} on ${submitterTeamName}
on ${submitterShiftDate} from ${submitterShiftStartTime}-${submitterShiftEndTime}
<#list rshifts as rshift>
with ${rshift.owner.name} shift as ${rshift.shiftSkillName} on ${rshift.shiftTeamName}
on ${rshift.shiftDate} from ${rshift.shiftStartTime}-${rshift.shiftEndTime}
</#list>
is pending for your approval.
To approve, reply Yes, To reject, reply No. Do not change the email subject.