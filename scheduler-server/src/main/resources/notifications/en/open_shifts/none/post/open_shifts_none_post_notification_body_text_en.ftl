<#assign shiftList = shiftListString?eval> 
 You are qualified to work the following open shifts:

<#list shiftList as shift>
    ${shift.skillName} on ${shift.team} on ${shift.shiftDate} from ${shift.shiftStartTime}-${shift.shiftEndTime}

</#list>


<#if shiftListOverflow == "true" >
  There are more open shifts available to you.
</#if>

