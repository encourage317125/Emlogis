SELECT        dbo.T_SiteSchedule.Name AS ScheduleName, dbo.T_Site.Name AS SiteName, dbo.T_Team.TeamID AS TeamId, dbo.T_Team.Name AS TeamName, 
                         dbo.T_Employee.EmployeeID, dbo.T_Employee.EmployeeIdentifier, dbo.T_SiteRequirement.SiteRequirementID, dbo.T_SiteRequirement.Date AS ShiftDate, 
                         dbo.T_Shift.StartTime, dbo.T_Shift.EndTime, dbo.T_ShiftGroup.PaidHours, dbo.T_Shift.ShiftType, dbo.T_Shift.ShiftID, 
                         dbo.T_SiteSchedule.Status AS ScheduleStatus
FROM            dbo.T_SiteScheduleAssignment INNER JOIN
                         dbo.T_Site INNER JOIN
                         dbo.T_SiteSchedule ON dbo.T_Site.SiteID = dbo.T_SiteSchedule.SiteID INNER JOIN
                         dbo.T_SiteRequirement ON dbo.T_SiteSchedule.SiteScheduleID = dbo.T_SiteRequirement.SiteScheduleID INNER JOIN
                         dbo.T_SiteResource ON dbo.T_SiteSchedule.SiteScheduleID = dbo.T_SiteResource.SiteScheduleID INNER JOIN
                         dbo.T_Employee ON dbo.T_Site.SiteID = dbo.T_Employee.SiteID INNER JOIN
                         dbo.T_Team ON dbo.T_Employee.HomeTeamID = dbo.T_Team.TeamID ON dbo.T_SiteScheduleAssignment.SiteScheduleID = dbo.T_SiteSchedule.SiteScheduleID AND
                          dbo.T_SiteScheduleAssignment.SiteRequirementID = dbo.T_SiteRequirement.SiteRequirementID AND 
                         dbo.T_SiteScheduleAssignment.SiteResourceID = dbo.T_SiteResource.SiteResourceID INNER JOIN
                         dbo.T_SiteShift ON dbo.T_Site.SiteID = dbo.T_SiteShift.SiteID AND dbo.T_SiteRequirement.SiteShiftID = dbo.T_SiteShift.SiteShiftID INNER JOIN
                         dbo.T_ShiftGroup INNER JOIN
                         dbo.T_Shift ON dbo.T_ShiftGroup.ShiftGroupID = dbo.T_Shift.ShiftGroupID ON dbo.T_SiteShift.ShiftID = dbo.T_Shift.ShiftID
WHERE        (dbo.T_SiteSchedule.Status = 1) AND (dbo.T_Employee.IsDeleted = 0) AND (dbo.T_Employee.IsSchedulable = 1) AND (dbo.T_Shift.ShiftType = 1)