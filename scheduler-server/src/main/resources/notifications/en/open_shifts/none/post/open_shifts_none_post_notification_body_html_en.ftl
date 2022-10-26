<#assign shiftList = shiftListString?eval>
<!DOCTYPE HTML>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    </head>
    <body>
        <p>You are qualified to work the following open shifts:</p>
        
     <#list shiftList as shift>
      <p>${shift.skillName} on ${shift.team} on ${shift.shiftDate} from ${shift.shiftStartTime}-${shift.shiftEndTime} </p>
      <p></p>
      </#list>
      <p>
	<#if shiftListOverflow == "true" >
	  There are more open shifts available to you.
	</#if>

	</p>
    </body>
</html>
